# 📱 Pointage RH – Application Mobile Android

**Pointage RH** est une application mobile Android de gestion du personnel en entreprise. Elle permet à différents types d'utilisateurs (Admin, Utilisateur) de gérer :

- ✅ Le pointage des employés
- 📆 Les plannings de travail
- 🌴 Les demandes et soldes de congés
- 👤 Les profils utilisateurs

## 🔐 Authentification & Rôles

L’application utilise **Firebase Authentication** pour gérer les connexions sécurisées, avec un système de rôles stockés dans Firebase Realtime Database :

- **Admin** : Gère les utilisateurs, les plannings, les absences, et toutes les demandes.
- **Utilisateur** : Consulte son planning, demande des congés, et gère son profil.

## 🛠️ Technologies utilisées

- **Langage** : Java
- **Interface** : Android XML (Android Studio)
- **Base de données** : Firebase Realtime Database
- **Authentification** : Firebase Auth
- **Architecture** : Basée sur des activités selon les rôles + Firebase

## 📂 Fonctionnalités principales

### 👤 Utilisateur
- Connexion sécurisée
- Consultation de son planning
- Demande de congé avec suivi d'état

### 👨‍💼 Admin & RH
- Gestion des utilisateurs (Ajout, modification, suppression, définition de rôles)
- Attribution des plannings
- Suivi des absences et présences
- Traitement des demandes de congé
- Marquage des absences (justifiées / non justifiées)




