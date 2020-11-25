package com.example.letsauthenticate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmailId, mPassword, mMobno;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressbar;

    FirebaseFirestore fstore;

    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.register_new_full_name);
        mEmailId = findViewById(R.id.register_new_email_id);
        mMobno = findViewById(R.id.register_new_mob_no);
        mPassword = findViewById(R.id.register_new_password);
        mRegisterBtn = findViewById(R.id.register_button);
        mLoginBtn = findViewById(R.id.register_login_here);
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        progressbar = findViewById(R.id.register_progressBar);

        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmailId.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String full_name = mFullName.getText().toString();
                final String mob_no = mMobno.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmailId.setError("Email is reqired.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required.");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must be >= 6 characters.");
                    return;
                }

                if (TextUtils.isEmpty(full_name)) {
                    mFullName.setError("Name is required");
                    return;
                }

                if (TextUtils.isEmpty(mob_no)) {
                    mMobno.setError("Mobile no is required");
                    return;
                }
                progressbar.setVisibility(View.VISIBLE);

                //register the user in firebase


                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //SEND VERIFICATION LINK

                            FirebaseUser fuser = fAuth.getCurrentUser();
                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "Email verification Link has been send", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure : Email not send " + e.getMessage());
                                }
                            });

                            Toast.makeText(Register.this, "User created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fstore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fname", full_name);
                            user.put("email", email);
                            user.put("mobno", mob_no);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "On succes user profile is created for " + userID);
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(Register.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }
}