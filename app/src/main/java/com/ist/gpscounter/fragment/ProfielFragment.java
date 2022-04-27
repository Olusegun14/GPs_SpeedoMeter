package com.ist.gpscounter.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.ist.gpscounter.activity.LoginActivity;
import com.ist.onlinemovieapplication.databinding.FragmentProfileBinding;
import com.ist.gpscounter.model.UserModel;

import java.util.Objects;

public class ProfielFragment extends Fragment {

    
    FragmentProfileBinding binding;
    private FirebaseFirestore database;

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        String userId = FirebaseAuth.getInstance().getUid();

        database = FirebaseFirestore.getInstance();


        database.collection("users")
                .document(userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserModel user = documentSnapshot.toObject(UserModel.class);

                if (user!=null){
                    Objects.requireNonNull(binding.username.getEditText()).setText(user.getUserEmail());
                    Objects.requireNonNull(binding.userAge.getEditText()).setText(user.getUserAge());

                    Objects.requireNonNull(binding.mobileNumber.getEditText()).setText(user.getUserPhone());
                    Objects.requireNonNull(binding.email.getEditText()).setText(user.getUserEmail());
                    binding.fullname.setText(user.getUserName());
                    binding.number.setText(user.getUserPhone());

                    Glide.with(getActivity())
                            .load(user.getUserProfile())
                            .into(binding.profileImage);

                    Log.d("USER_IMAGE",user.getUserProfile());

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {

            }
        });


        binding.submitContactBtn.setOnClickListener(view -> {
            getActivity().finish();
            Intent i=new Intent(getActivity(), LoginActivity.class);
            i.putExtra("finish", true);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            //startActivity(i);
            startActivity( i );
            FirebaseAuth.getInstance().signOut();
        });
        return binding.getRoot();

    }

}
