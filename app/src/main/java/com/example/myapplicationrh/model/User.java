package com.example.myapplicationrh.model;

import java.io.Serializable;

public class User implements Serializable {
    public String uid;
    public String email;
    public String role;
    public String nom;
    public String prenom;
    public String telephone; // Utiliser "telephone" partout

    public User() {} // Constructeur vide requis par Firebase

    public User(String uid, String email, String role, String nom, String prenom, String telephone) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    // Getters et setters

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
