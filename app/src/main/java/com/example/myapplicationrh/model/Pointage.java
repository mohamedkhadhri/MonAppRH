package com.example.myapplicationrh.model;

public class Pointage {
    public String date;
    public String heureEntree;
    public String heureSortie;

    public Pointage() {} // constructeur vide requis par Firebase

    public Pointage(String date, String heureEntree, String heureSortie) {
        this.date = date;
        this.heureEntree = heureEntree;
        this.heureSortie = heureSortie;
    }
}

