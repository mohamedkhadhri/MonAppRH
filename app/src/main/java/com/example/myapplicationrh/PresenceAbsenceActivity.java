package com.example.myapplicationrh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PresenceAbsenceActivity extends AppCompatActivity {

    private ListView listUsers;
    private DatabaseReference usersRef;
    private ArrayList<String> emails = new ArrayList<>();
    private HashMap<String, String> userIdByEmail = new HashMap<>(); // email -> userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence_absence);

        listUsers = findViewById(R.id.listUsers);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        chargerUtilisateurs();

        listUsers.setOnItemClickListener((parent, view, position, id) -> {
            String email = emails.get(position);
            String userId = userIdByEmail.get(email);

            Intent intent = new Intent(this, DetailPresenceActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("email", email);
            startActivity(intent);
        });
    }

    private void chargerUtilisateurs() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emails.clear();
                userIdByEmail.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        emails.add(email);
                        userIdByEmail.put(email, userSnapshot.getKey());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(PresenceAbsenceActivity.this,
                        android.R.layout.simple_list_item_1, emails);
                listUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PresenceAbsenceActivity.this, "Erreur chargement utilisateurs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
