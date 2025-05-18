package com.example.myapplicationrh;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CongeActivity extends AppCompatActivity {

    private EditText editDateDebut, editDateFin, editRaison;
    private Button btnEnvoyerDemande;
    private LinearLayout layoutDemandes;
    private DatabaseReference mDatabase;
    private String userId;

    private Calendar dateDebutCalendar, dateFinCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conge);

        editDateDebut = findViewById(R.id.editDateDebut);
        editDateFin = findViewById(R.id.editDateFin);
        editRaison = findViewById(R.id.editRaison);
        btnEnvoyerDemande = findViewById(R.id.btnEnvoyerDemande);
        layoutDemandes = findViewById(R.id.layoutDemandes);

        editDateDebut.setFocusable(false);
        editDateDebut.setClickable(true);
        editDateFin.setFocusable(false);
        editDateFin.setClickable(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editDateDebut.setOnClickListener(v -> showDatePicker(true));
        editDateFin.setOnClickListener(v -> showDatePicker(false));

        btnEnvoyerDemande.setOnClickListener(v -> {
            String dateDebut = editDateDebut.getText().toString().trim();
            String dateFin = editDateFin.getText().toString().trim();
            String raison = editRaison.getText().toString().trim();

            if (dateDebut.isEmpty() || dateFin.isEmpty() || raison.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dateDebutCalendar != null && dateFinCalendar != null &&
                    dateFinCalendar.before(dateDebutCalendar)) {
                Toast.makeText(this, "La date de fin doit être après la date de début", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> demande = new HashMap<>();
            demande.put("dateDebut", dateDebut);
            demande.put("dateFin", dateFin);
            demande.put("raison", raison);
            demande.put("status", "en attente");

            mDatabase.child("demandesConge").child(userId).push().setValue(demande)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Demande envoyée", Toast.LENGTH_SHORT).show();
                        editDateDebut.setText("");
                        editDateFin.setText("");
                        editRaison.setText("");
                        dateDebutCalendar = null;
                        dateFinCalendar = null;
                        afficherDemandes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        afficherDemandes();
    }

    private void showDatePicker(boolean isDebut) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateStr = sdf.format(selectedDate.getTime());

                    if (isDebut) {
                        editDateDebut.setText(dateStr);
                        dateDebutCalendar = selectedDate;
                    } else {
                        editDateFin.setText(dateStr);
                        dateFinCalendar = selectedDate;
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Désactive les dates passées
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        // Si on choisit une date de fin, elle ne doit pas être avant la date de début
        if (!isDebut && dateDebutCalendar != null) {
            datePickerDialog.getDatePicker().setMinDate(dateDebutCalendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void afficherDemandes() {
        layoutDemandes.removeAllViews();

        mDatabase.child("demandesConge").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    TextView tv = new TextView(CongeActivity.this);
                    tv.setText("Aucune demande de congé trouvée.");
                    layoutDemandes.addView(tv);
                    return;
                }

                for (DataSnapshot demandeSnapshot : snapshot.getChildren()) {
                    String dateDebut = demandeSnapshot.child("dateDebut").getValue(String.class);
                    String dateFin = demandeSnapshot.child("dateFin").getValue(String.class);
                    String status = demandeSnapshot.child("status").getValue(String.class);
                    String causeRefus = demandeSnapshot.child("causeRefus").getValue(String.class);

                    StringBuilder message = new StringBuilder();
                    message.append("Du ").append(dateDebut)
                            .append(" au ").append(dateFin)
                            .append(" - Statut : ").append(status);

                    if ("refusé".equalsIgnoreCase(status) && causeRefus != null) {
                        message.append("\nCause du refus : ").append(causeRefus);
                    }

                    TextView tv = new TextView(CongeActivity.this);
                    tv.setText(message.toString());
                    tv.setPadding(0, 8, 0, 8);
                    layoutDemandes.addView(tv);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CongeActivity.this, "Erreur de chargement : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
