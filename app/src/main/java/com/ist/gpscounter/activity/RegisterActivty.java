package com.ist.gpscounter.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ist.onlinemovieapplication.databinding.ActivityRegisterBinding;
import com.ist.gpscounter.model.UserModel;

import java.util.Objects;
import java.util.UUID;

public class RegisterActivty extends AppCompatActivity {


    ActivityRegisterBinding binding;
    private static final int PICK_IMAGE_REQUEST = 100;

    FirebaseAuth auth;
    FirebaseFirestore database;
    Uri fileImagePath;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();




        binding.loginLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivty.this, LoginActivity.class));

            }
        });


         binding.profileImage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType( "image/*" );
                intent.setAction( Intent.ACTION_GET_CONTENT );
                startActivityForResult( Intent.createChooser( intent, "select  image" ), PICK_IMAGE_REQUEST );
            }
        } );

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = Objects.requireNonNull(binding.username.getEditText()).getText().toString();
                String userAge = Objects.requireNonNull(binding.userAge.getEditText()).getText().toString();
                String userPhone = Objects.requireNonNull(binding.mobileNumber.getEditText()).getText().toString();
                String userEmail = Objects.requireNonNull(binding.email.getEditText()).getText().toString();
                String userPassword = Objects.requireNonNull(binding.password.getEditText()).getText().toString();

                String countyCode = binding.ccp.getSelectedCountryCode();
                Log.d("registerBtn", countyCode);
                if (userName.isEmpty()) {
                    binding.username.setError("Name is required");
                    return;
                }
                if (userAge.isEmpty()) {
                    binding.userAge.setError("Age is required");
                    binding.username.setErrorEnabled(false);
                    return;
                }
                if (userEmail.isEmpty()) {
                    binding.email.setError("Email is required");
                    binding.userAge.setErrorEnabled(false);

                    return;
                }

                if (userPassword.isEmpty()) {
                    binding.password.setError("Password is required");
                    binding.mobileNumber.setErrorEnabled(false);
                }if (fileImagePath==null) {
                    Toast.makeText(RegisterActivty.this, "Image is required", Toast.LENGTH_SHORT).show();
                } else {
                    binding.password.setErrorEnabled(false);
                    userRegister(userName, userAge, userEmail, userPhone, userPassword);
                }
            }
        });



    }

    private void userRegister(String userName, String userAge, String userEmail, String userPhone, String userPassword) {


        binding.progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    String uid = Objects.requireNonNull(task.getResult()).getUser().getUid();
                    uploadImage( uid, userName,userAge,userEmail,userPhone,userPassword);

                    /*database.collection("users")
                            .document(uid)
                            .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                binding.progressBar.setVisibility(View.GONE);

                                Log.d( "isSuccessful","isSuccessful" );
                                if (fileImagePath!=null){


                                }


                            } else {
                                binding.progressBar.setVisibility(View.GONE);

                                Log.d("ERROR",task.getException().getLocalizedMessage());
                                Toast.makeText(RegisterActivty.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });*/
                } else {
                    binding.progressBar.setVisibility(View.GONE);

                    Log.d("ERROR",task.getException().getLocalizedMessage());
                    Toast.makeText(RegisterActivty.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            fileImagePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap( getContentResolver(), fileImagePath );
                binding.profileImage.setImageBitmap( bitmap );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void uploadImage(String uid, String userName, String userAge, String userEmail, String userPhone, String userPassword) {



        // Defining the child of storageReference
        StorageReference ref = storageReference.child(
                "images/" + UUID.randomUUID().toString() );

        // adding listeners on upload
        // or failure of image
        ref.putFile(fileImagePath).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    final UserModel user = new UserModel(userName, userAge, userEmail, userPhone,userPassword,uri.toString());
                                    UploadDataToFireStore(uid,user );
                                }
                            }
                        } );
                    }
                } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d( "addOnFailureListener", e.getLocalizedMessage() + "error" );
            }

        } );

    }


    private void UploadDataToFireStore( String uid,UserModel user) {
        database.collection( "users" )
                .document(uid)
                .set(user ).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startActivity( new Intent( RegisterActivty.this, MainActivity.class ) );
                    finish();
                } else {
                    Toast.makeText( RegisterActivty.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT ).show();
                }
            }
        } ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }



}