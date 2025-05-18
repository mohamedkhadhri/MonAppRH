package com.example.myapplicationrh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationrh.UsersAdapter;
import com.example.myapplicationrh.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UsersAdapter adapter;
    private List<User> userList;
    private DatabaseReference usersRef;
    private Button buttonAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        buttonAddUser = findViewById(R.id.buttonAddUser);

        userList = new ArrayList<>();
        adapter = new UsersAdapter(this, userList, new UsersAdapter.OnUserActionListener() {
            @Override

            public void onEditClicked(User user, int position) {
                Intent intent = new Intent(UserListActivity.this, EditUserActivity.class);
                intent.putExtra("selectedUser", user);
                startActivity(intent);
            }


            @Override
            public void onDeleteClicked(User user, int position) {
                deleteUser(user);
            }
        });

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsersFromFirebase();

        buttonAddUser.setOnClickListener(v -> {
            // TODO: Lancer activité d'ajout utilisateur
            Intent intent = new Intent(UserListActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loadUsersFromFirebase() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserListActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(User user) {
        if (user.uid != null) {
            usersRef.child(user.uid).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(UserListActivity.this, "Utilisateur supprimé", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(UserListActivity.this, "Erreur suppression: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
