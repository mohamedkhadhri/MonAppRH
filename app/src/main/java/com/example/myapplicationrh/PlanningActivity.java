package com.example.myapplicationrh;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PlanningActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView textPointageDuJour;

    private DatabaseReference pointagesRef;
    private DatabaseReference usersRef;
    private String currentUserId;
    private String role = "user"; // valeur par défaut

    private HashMap<String, String> userEmailMap = new HashMap<>(); // userId => email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        calendarView = findViewById(R.id.calendarView);
        textPointageDuJour = findViewById(R.id.textPointageDuJour);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        pointagesRef = FirebaseDatabase.getInstance().getReference("pointages");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Récupérer le rôle de l'utilisateur connecté
        usersRef.child(currentUserId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                role = snapshot.getValue(String.class);
                if (role == null) role = "user";

                if (role.equals("admin")) {
                    chargerTousLesEmails(() -> {
                        String dateActuelle = getDateFromMillis(calendarView.getDate());
                        chargerPointagesAdmin(dateActuelle);
                    });
                } else {
                    String dateActuelle = getDateFromMillis(calendarView.getDate());
                    chargerPointagesUser(dateActuelle);
                }

                calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                    String dateSelectionnee = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (role.equals("admin")) {
                        chargerPointagesAdmin(dateSelectionnee);
                    } else {
                        chargerPointagesUser(dateSelectionnee);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlanningActivity.this, "Erreur lecture rôle : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ADMIN : charge les emails, puis les pointages de tous
    private void chargerTousLesEmails(Runnable callback) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userEmailMap.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (email != null) {
                        userEmailMap.put(userId, email);
                    }
                }
                callback.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlanningActivity.this, "Erreur chargement emails : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ADMIN : charger tous les pointages
    private void chargerPointagesAdmin(String date) {
        pointagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotPointages) {
                StringBuilder sb = new StringBuilder();
                sb.append("Pointages du ").append(date).append(" :\n\n");

                boolean pointageTrouve = false;

                for (DataSnapshot userSnapshot : snapshotPointages.getChildren()) {
                    String userId = userSnapshot.getKey();
                    DataSnapshot pointageDate = userSnapshot.child(date);

                    if (pointageDate.exists()) {
                        String heureEntree = pointageDate.child("heureEntree").getValue(String.class);
                        String heureSortie = pointageDate.child("heureSortie").getValue(String.class);

                        String email = userEmailMap.getOrDefault(userId, "Utilisateur inconnu");

                        sb.append("Email : ").append(email).append("\n");
                        sb.append("  ➤ Entrée: ").append(heureEntree != null ? heureEntree : "--").append("\n");
                        sb.append("  ➤ Sortie: ").append(heureSortie != null ? heureSortie : "--").append("\n\n");

                        pointageTrouve = true;
                    }
                }

                if (!pointageTrouve) {
                    textPointageDuJour.setText("Aucun pointage trouvé pour le " + date);
                } else {
                    textPointageDuJour.setText(sb.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlanningActivity.this, "Erreur lecture pointages : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // UTILISATEUR : charger uniquement ses pointages
    private void chargerPointagesUser(String date) {
        pointagesRef.child(currentUserId).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String heureEntree = snapshot.child("heureEntree").getValue(String.class);
                    String heureSortie = snapshot.child("heureSortie").getValue(String.class);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Votre pointage du ").append(date).append(" :\n\n");
                    sb.append("  ➤ Entrée : ").append(heureEntree != null ? heureEntree : "--").append("\n");
                    sb.append("  ➤ Sortie : ").append(heureSortie != null ? heureSortie : "--").append("\n");

                    textPointageDuJour.setText(sb.toString());
                } else {
                    textPointageDuJour.setText("Aucun pointage trouvé pour le " + date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlanningActivity.this, "Erreur lecture pointage : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getDateFromMillis(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
