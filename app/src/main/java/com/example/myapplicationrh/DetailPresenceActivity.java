package com.example.myapplicationrh;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.ResolveInfo;


import java.text.SimpleDateFormat;
import java.util.*;

public class DetailPresenceActivity extends AppCompatActivity {

    private TextView textEmail;
    private LinearLayout layoutPointages, layoutAbsences;
    private DatabaseReference pointagesRef, justificationsRef;
    private String userId, email;
    private List<String> joursDuMois;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_presence);
        verifierAppsMail();

        textEmail = findViewById(R.id.textEmail);
        layoutPointages = findViewById(R.id.layoutPointages);
        layoutAbsences = findViewById(R.id.layoutAbsences);

        pointagesRef = FirebaseDatabase.getInstance().getReference("pointages");
        justificationsRef = FirebaseDatabase.getInstance().getReference("justifications");

        // Récupération des données de l'utilisateur
        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        textEmail.setText("Historique de : " + email);

        joursDuMois = genererDatesMoisEnCours();
        chargerPointages();
    }

    private void chargerPointages() {
        pointagesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layoutPointages.removeAllViews();
                layoutAbsences.removeAllViews();

                for (String date : joursDuMois) {
                    DataSnapshot jourSnapshot = snapshot.child(date);

                    if (jourSnapshot.exists()) {
                        String entree = jourSnapshot.child("heureEntree").getValue(String.class);
                        String sortie = jourSnapshot.child("heureSortie").getValue(String.class);
                        ajouterVuePointage(date, entree, sortie);
                    } else {
                        ajouterVueAbsence(date);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailPresenceActivity.this, "Erreur chargement : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ajouterVuePointage(String date, String entree, String sortie) {
        TextView tv = new TextView(this);
        tv.setText("📅 " + date + "\n➡️ Entrée : " + (entree != null ? entree : "--") + " | Sortie : " + (sortie != null ? sortie : "--"));
        tv.setPadding(8, 8, 8, 8);
        layoutPointages.addView(tv);
    }

    private void ajouterVueAbsence(String date) {
        LinearLayout ligne = new LinearLayout(this);
        ligne.setOrientation(LinearLayout.HORIZONTAL);
        ligne.setPadding(8, 8, 8, 8);

        TextView tvDate = new TextView(this);
        tvDate.setText("📅 " + date);
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button btnJustifier = new Button(this);
        btnJustifier.setText("Justifier");
        btnJustifier.setOnClickListener(v -> afficherPopupJustification(date));

        ligne.addView(tvDate);
        ligne.addView(btnJustifier);
        layoutAbsences.addView(ligne);
    }

    private void afficherPopupJustification(String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter justification pour " + date);
        String[] options = {"Maladie", "Demande d'explication"};

        builder.setItems(options, (dialog, which) -> {
            String motif = options[which];
            enregistrerJustification(date, motif);
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void enregistrerJustification(String date, String motif) {
        Map<String, String> data = new HashMap<>();
        data.put("motif", motif);
        data.put("date", date);

        // Enregistrer dans la section "justifications"
        justificationsRef.child(userId).child(date).setValue(data)
                .addOnSuccessListener(unused -> {
                    if (motif.equals("Maladie")) {
                        // Écrire dans "pointages" comme une fausse entrée/sortie avec "Maladie"
                        Map<String, String> fakePointage = new HashMap<>();
                        fakePointage.put("heureEntree", "Maladie");
                        fakePointage.put("heureSortie", "Maladie");

                        pointagesRef.child(userId).child(date).setValue(fakePointage)
                                .addOnSuccessListener(unused2 -> {
                                    Toast.makeText(this, "Jour justifié comme 'Maladie'", Toast.LENGTH_SHORT).show();
                                    chargerPointages(); // Refresh affichage
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else if (motif.equals("Demande d'explication")) {
                        Toast.makeText(this, "Justification enregistrée", Toast.LENGTH_SHORT).show();
                        ouvrirClientMail(date, email);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur lors de l'enregistrement : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void ouvrirClientMail(String date, String emailUtilisateur) {
        String sujet = "Demande d'explication pour absence du " + date;
        String corps = "Bonjour,\n\nJe souhaite fournir une explication pour ton absence le " + date + ".\n\nMerci de prendre en compte ma demande.\n\nCordialement,";

        // Intent 1 : ACTION_SENDTO + mailto
        Intent intentSendTo = new Intent(Intent.ACTION_SENDTO);
        intentSendTo.setData(Uri.parse("mailto:" + emailUtilisateur));
        intentSendTo.putExtra(Intent.EXTRA_SUBJECT, sujet);
        intentSendTo.putExtra(Intent.EXTRA_TEXT, corps);

        if (intentSendTo.resolveActivity(getPackageManager()) != null) {
            startActivity(intentSendTo);
            return;
        }

        // Si pas d'app qui répond à ACTION_SENDTO, on tente ACTION_SEND + message/rfc822
        Intent intentSend = new Intent(Intent.ACTION_SEND);
        intentSend.setType("message/rfc822");
        intentSend.putExtra(Intent.EXTRA_EMAIL, new String[]{emailUtilisateur});
        intentSend.putExtra(Intent.EXTRA_SUBJECT, sujet);
        intentSend.putExtra(Intent.EXTRA_TEXT, corps);

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intentSend, 0);
        if (!resInfo.isEmpty()) {
            // Affichage debug - liste des apps mail trouvées
            StringBuilder appsFound = new StringBuilder("Apps mail trouvées : ");
            for (ResolveInfo info : resInfo) {
                appsFound.append(info.activityInfo.packageName).append(", ");
            }
            Toast.makeText(this, appsFound.toString(), Toast.LENGTH_LONG).show();

            startActivity(Intent.createChooser(intentSend, "Choisir une application mail"));
        } else {
            Toast.makeText(this, "Aucune application mail trouvée sur cet appareil", Toast.LENGTH_LONG).show();
        }
    }


    private void verifierAppsMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));

        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, 0);

        if (resolveInfos.isEmpty()) {
            Toast.makeText(this, "Aucune application mail installée sur cet appareil", Toast.LENGTH_LONG).show();
        } else {
            StringBuilder sb = new StringBuilder("Apps mail trouvées:\n");
            for (ResolveInfo info : resolveInfos) {
                sb.append(info.activityInfo.packageName).append("\n");
            }
            Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        }
    }





    private List<String> genererDatesMoisEnCours() {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int mois = calendar.get(Calendar.MONTH);
        int annee = calendar.get(Calendar.YEAR);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int jour = 1; jour <= today; jour++) {
            calendar.set(annee, mois, jour);
            dates.add(sdf.format(calendar.getTime()));
        }

        return dates;
    }

}
