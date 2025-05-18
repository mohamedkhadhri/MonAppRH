package com.example.myapplicationrh.model;


public class DemandeConge {
    private String id;
    private String userId;
    private String dateDebut;
    private String dateFin;
    private String status;
    private String causeRefus;
    private String raison;

    public DemandeConge() {
        // Constructeur vide requis pour Firebase
    }

    // getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }

    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCauseRefus() { return causeRefus; }
    public void setCauseRefus(String causeRefus) { this.causeRefus = causeRefus; }

    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }
}
