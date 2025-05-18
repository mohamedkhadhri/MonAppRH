package com.example.myapplicationrh;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplicationrh.model.DemandeConge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminDemandeCongeActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDemandes;
    private ArrayList<DemandeConge> demandesList = new ArrayList<>();
    private DemandeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_demande_conge);

        recyclerViewDemandes = findViewById(R.id.recyclerViewDemandes);
        recyclerViewDemandes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DemandeAdapter();
        recyclerViewDemandes.setAdapter(adapter);

        loadDemandes();
    }

    private void loadDemandes() {
        DatabaseReference demandesRef = FirebaseDatabase.getInstance().getReference("demandesConge");

        demandesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                demandesList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot demandeSnapshot : userSnapshot.getChildren()) {
                        DemandeConge demande = demandeSnapshot.getValue(DemandeConge.class);
                        if (demande != null) {
                            demande.setId(demandeSnapshot.getKey());
                            demande.setUserId(userSnapshot.getKey());
                            demandesList.add(demande);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if (demandesList.isEmpty()) {
                    Toast.makeText(AdminDemandeCongeActivity.this, "Aucune demande de congé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDemandeCongeActivity.this, "Erreur chargement demandes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class DemandeAdapter extends RecyclerView.Adapter<DemandeAdapter.DemandeViewHolder> {

        @NonNull
        @Override
        public DemandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_demande_conge, parent, false);
            return new DemandeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DemandeViewHolder holder, int position) {
            DemandeConge demande = demandesList.get(position);

            holder.textRaison.setText("Raison : " + demande.getRaison());
            holder.textDates.setText("Du " + demande.getDateDebut() + " au " + demande.getDateFin());
            holder.textStatus.setText("Statut : " + demande.getStatus());


            if (demande.getUserId() != null) {
                getEmailFromUserId(demande.getUserId(), email -> {
                    if (email != null) {
                        holder.textUserId.setVisibility(View.VISIBLE);
                        holder.textUserId.setText("Utilisateur : " + email);
                    } else {
                        holder.textUserId.setVisibility(View.GONE);
                    }
                });
            } else {
                holder.textUserId.setVisibility(View.GONE);
            }

            boolean isPending = "en attente".equalsIgnoreCase(demande.getStatus());

            holder.btnAccepter.setEnabled(isPending);
            holder.btnRefuser.setEnabled(isPending);

            holder.btnAccepter.setOnClickListener(v -> {
                DatabaseReference demandeRef = FirebaseDatabase.getInstance()
                        .getReference("demandesConge")
                        .child(demande.getUserId())
                        .child(demande.getId());

                demandeRef.child("status").setValue("accepté");
                demandeRef.child("causeRefus").setValue("");

                Toast.makeText(AdminDemandeCongeActivity.this, "Demande acceptée", Toast.LENGTH_SHORT).show();
            });

            holder.btnRefuser.setOnClickListener(v -> showRefusDialog(demande));
        }

        @Override
        public int getItemCount() {
            return demandesList.size();
        }

        class DemandeViewHolder extends RecyclerView.ViewHolder {
            TextView textRaison, textDates, textStatus, textCauseRefus, textUserId;
            Button btnAccepter, btnRefuser;

            public DemandeViewHolder(@NonNull View itemView) {
                super(itemView);
                textRaison = itemView.findViewById(R.id.textRaison);
                textDates = itemView.findViewById(R.id.textDates);
                textStatus = itemView.findViewById(R.id.textStatus);
                textCauseRefus = itemView.findViewById(R.id.textCauseRefus);
                textUserId = itemView.findViewById(R.id.textUserId);
                btnAccepter = itemView.findViewById(R.id.btnAccepter);
                btnRefuser = itemView.findViewById(R.id.btnRefuser);
            }
        }
    }

    private void showRefusDialog(DemandeConge demande) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Motif du refus");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);

        builder.setPositiveButton("Refuser", (dialog, which) -> {
            String cause = input.getText().toString().trim();
            if (cause.isEmpty()) {
                Toast.makeText(AdminDemandeCongeActivity.this, "Veuillez saisir un motif de refus", Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference demandeRef = FirebaseDatabase.getInstance()
                        .getReference("demandesConge")
                        .child(demande.getUserId())
                        .child(demande.getId());

                demandeRef.child("status").setValue("refusé");
                demandeRef.child("causeRefus").setValue(cause);

                Toast.makeText(AdminDemandeCongeActivity.this, "Demande refusée", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void getEmailFromUserId(String userId, EmailCallback callback) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.getValue(String.class);
                callback.onEmailReceived(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onEmailReceived(null);
            }
        });
    }


    interface EmailCallback {
        void onEmailReceived(String email);
    }
}
