package com.example.myapplicationrh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");


        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {

            String uid = currentUser.getUid();
            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if (role == null) {
                            Toast.makeText(LoginActivity.this, "Rôle non défini", Toast.LENGTH_SHORT).show();

                            setContentView(R.layout.activity_login);
                            initUI();
                        } else {
                            redirectUser(role);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                        setContentView(R.layout.activity_login);
                        initUI();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(LoginActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.activity_login);
                    initUI();
                }
            });
        } else {

            setContentView(R.layout.activity_login);
            initUI();
        }
    }


    private void initUI() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();
                            userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        if (role == null) {
                                            Toast.makeText(LoginActivity.this, "Rôle non défini", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        redirectUser(role);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Utilisateur non trouvé", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Échec de la connexion : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        buttonRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }


    private void redirectUser(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(LoginActivity.this, AdminActivity.class);
                break;
            case "user":
                intent = new Intent(LoginActivity.this, UserActivity.class);
                break;
            default:
                Toast.makeText(this, "Rôle inconnu", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }
}
