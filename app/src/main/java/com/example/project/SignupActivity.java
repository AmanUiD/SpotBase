package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project.Model.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
      TextView login;

    TextInputEditText name,profession,email,password;
      Button SignUp;
      FirebaseAuth auth;
      FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        login = findViewById(R.id.login);
        name = findViewById(R.id.nameText);
        profession = findViewById(R.id.profession);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        SignUp = findViewById(R.id.signUpBtn);

        //FirebaseAuth
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                auth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        String Name = name.getText().toString();
                        String Profession = profession.getText().toString();
                        String emailText = email.getText().toString();
                        String Password = password.getText().toString();

                        if (task.isSuccessful()){
                            UserData userData = new UserData(Name,Profession,emailText,Password);
                            String id = task.getResult().getUser().getUid();
                            database.getReference().child("Users").child(id).setValue(userData);
                            Toast.makeText(SignupActivity.this, "User Data Saved", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            Toast.makeText(SignupActivity.this, "Error", Toast.LENGTH_SHORT).show();

                        }
                    }

                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}