package com.ist.gpscounter.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.ist.onlinemovieapplication.databinding.ActivityForgotPasswordBinding;

import java.util.Objects;

public class ForgotPasswordActivty extends AppCompatActivity {


    ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        binding.resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = Objects.requireNonNull(binding.email.getEditText()).getText().toString();

                if (userEmail.isEmpty()) {
                    binding.email.setError("email is required");
                } else {
                    binding.email.setErrorEnabled(false);
                    userFogotPassword(userEmail);
                }
            }
        });

    }


    private  void userFogotPassword(String userEmail){
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TAG", "Email sent.");
                                Toast.makeText(getApplicationContext(), "Check Your Email", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ForgotPasswordActivty.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


}
