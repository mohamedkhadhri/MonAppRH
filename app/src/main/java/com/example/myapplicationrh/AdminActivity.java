package com.example.myapplicationrh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    Button btnManageUsers, btnViewPlanning, btnViewConges, btnViewPresence, btnLogout;
    TextView textUserCount;
    ImageView userIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Liens vers les vues
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnViewPlanning = findViewById(R.id.btnViewPlanning);
        btnViewConges = findViewById(R.id.btnViewConges);
        btnViewPresence = findViewById(R.id.btnViewPresence);
        btnLogout = findViewById(R.id.btnLogout);
        textUserCount = findViewById(R.id.textUserCount);
        userIcon = findViewById(R.id.userIcon);


        userIcon.setImageResource(R.drawable.ic_user);


        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, UserListActivity.class));
        });


        btnViewPlanning.setOnClickListener(v -> {
            startActivity(new Intent(this, PlanningActivity.class));
        });


        btnViewConges.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminDemandeCongeActivity.class));
        });


        btnViewPresence.setOnClickListener(v -> {
            startActivity(new Intent(this, PresenceAbsenceActivity.class));
        });


        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });


        loadUserCount();
    }

    private void loadUserCount() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int userCount = 0;
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String role = userSnapshot.child("role").getValue(String.class);
                    if ("user".equalsIgnoreCase(role)) {
                        userCount++;
                    }
                }
                textUserCount.setText("Utilisateurs : " + userCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                textUserCount.setText("Erreur de chargement");
            }
        });
    }
}
