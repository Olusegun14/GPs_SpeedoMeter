package com.ist.gpscounter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ist.onlinemovieapplication.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    ActivityLoginBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore database;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();


        binding.createLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegisterActivty.class));

            }
        });

        binding.forgotPassBtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this,ForgotPasswordActivty.class));

        });


        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = Objects.requireNonNull(binding.email.getEditText()).getText().toString();
                String password = Objects.requireNonNull(binding.password.getEditText()).getText().toString();

                if (userEmail.isEmpty()) {
                    binding.email.setError("Name is required");
                    return;
                }
                if (password.isEmpty()) {
                    binding.password.setError("Age is required");
                    binding.email.setErrorEnabled(false);

                } else {
                    binding.password.setErrorEnabled(false);
                    userLogin(userEmail, password);
                }
            }
        });



    }


    private void userLogin(String userEmail,String userPassword){

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                binding.progressBar.setVisibility(View.GONE);

                if(task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                   finishAffinity();
                    updateUserPassword(userPassword);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.GONE);

            }
        });
    }


    private void updateUserPassword(String passsword) {
        String userId = FirebaseAuth.getInstance().getUid();
        assert userId != null;
        database.collection( "users" )
                .document(userId)
                .update( "userPassword" ,passsword).addOnCompleteListener( new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d( "onComplete", "success" );
                } else {
                    Log.d( "onComplete", "error" );
                }
            }
        } ).addOnFailureListener( e -> {
                    Log.d( "onComplete", e.getLocalizedMessage() );

                }
        );

    }

}