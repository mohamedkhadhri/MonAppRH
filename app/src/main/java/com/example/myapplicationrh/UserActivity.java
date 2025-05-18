package com.example.myapplicationrh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    private TextView textUserProfile, textHeureEntree, textHeureSortie;
    private Button btnShowPlanning, btnPointageEntree, btnPointageSortie, btnDemanderConge, btnLogout;

    private DatabaseReference mDatabase;
    private String userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialiser Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Récupérer utilisateur connecté Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Trouver les vues
        textUserProfile = findViewById(R.id.textUserProfile);
        btnShowPlanning = findViewById(R.id.btnShowPlanning);
        btnPointageEntree = findViewById(R.id.btnPointageEntree);
        btnPointageSortie = findViewById(R.id.btnPointageSortie);
        textHeureEntree = findViewById(R.id.textHeureEntree);
        textHeureSortie = findViewById(R.id.textHeureSortie);
        btnDemanderConge = findViewById(R.id.btnDemanderConge);
        btnLogout = findViewById(R.id.btnLogout);

        // Charger et afficher le nom complet de l'utilisateur depuis Firebase Realtime Database
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nom = snapshot.child("nom").getValue(String.class);
                    String prenom = snapshot.child("prenom").getValue(String.class);
                    String nomComplet = ((prenom != null) ? prenom : "") + " " + ((nom != null) ? nom : "");
                    textUserProfile.setText("Profil utilisateur : " + nomComplet.trim());
                } else {
                    textUserProfile.setText("Profil utilisateur : Inconnu");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, "Erreur chargement profil", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener bouton afficher planning
        btnShowPlanning.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, PlanningActivity.class);
            startActivity(intent);
        });

        // Listener pointage entrée
        btnPointageEntree.setOnClickListener(v -> {
            String heureEntree = getHeureActuelle();
            textHeureEntree.setText("Heure d'entrée : " + heureEntree);
            mDatabase.child("pointages").child(userId).child(getDateActuelle()).child("heureEntree").setValue(heureEntree);
        });

        // Listener pointage sortie
        btnPointageSortie.setOnClickListener(v -> {
            String heureSortie = getHeureActuelle();
            textHeureSortie.setText("Heure de sortie : " + heureSortie);
            mDatabase.child("pointages").child(userId).child(getDateActuelle()).child("heureSortie").setValue(heureSortie);
        });

        // Listener demander congé
        btnDemanderConge.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, CongeActivity.class);
            startActivity(intent);
        });

        // Listener logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(UserActivity.this, "Déconnecté avec succès", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String getHeureActuelle() {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String getDateActuelle() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
