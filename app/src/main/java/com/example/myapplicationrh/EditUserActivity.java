package com.example.myapplicationrh;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationrh.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditUserActivity extends AppCompatActivity {

    private EditText editEmail, editRole, editNom, editPrenom, editTelephone;
    private Button buttonSave;

    private DatabaseReference usersRef;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        // Récupérer tous les champs
        editEmail = findViewById(R.id.editEmail);
        editRole = findViewById(R.id.editRole);
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editTelephone = findViewById(R.id.editTelephone);
        buttonSave = findViewById(R.id.buttonSave);

        // Récupérer l'utilisateur passé par intent
        selectedUser = (User) getIntent().getSerializableExtra("selectedUser");

        if (selectedUser != null) {
            editEmail.setText(selectedUser.email);
            editRole.setText(selectedUser.role);
            editNom.setText(selectedUser.nom);
            editPrenom.setText(selectedUser.prenom);
            editTelephone.setText(selectedUser.telephone);
        } else {
            Toast.makeText(this, "Aucun utilisateur sélectionné", Toast.LENGTH_SHORT).show();
            finish();
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        buttonSave.setOnClickListener(v -> {
            String newEmail = editEmail.getText().toString().trim();
            String newRole = editRole.getText().toString().trim();
            String newNom = editNom.getText().toString().trim();
            String newPrenom = editPrenom.getText().toString().trim();
            String newTelephone = editTelephone.getText().toString().trim();

            if (newEmail.isEmpty() || newRole.isEmpty() || newNom.isEmpty() || newPrenom.isEmpty() || newTelephone.isEmpty()) {
                Toast.makeText(this, "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mettre à jour les données
            selectedUser.email = newEmail;
            selectedUser.role = newRole;
            selectedUser.nom = newNom;
            selectedUser.prenom = newPrenom;
            selectedUser.telephone = newTelephone;

            usersRef.child(selectedUser.uid).setValue(selectedUser)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Utilisateur mis à jour avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
