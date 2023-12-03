package model.secteur;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import connection.BddObject;
import model.etat.EtatSolaire;
import model.meteo.Meteo;
import model.panneau.Panneau;
import model.pointage.Pointage;

public class Secteur extends BddObject {

    String nom;
    Panneau panneau;
    Salle[] salles;

    public Salle[] getSalles() {
        return salles;
    }

    public void setSalles(Salle[] salles) throws IllegalArgumentException {
        if (salles.length <= 0) throw new IllegalArgumentException("Salles invalides");
        this.salles = salles;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) throws IllegalArgumentException {
        if (nom.isEmpty()) throw new IllegalArgumentException("Nom est vide");
        this.nom = nom;
    }

    public Panneau getPanneau() {
        return panneau;
    }

    public void setPanneau(Panneau panneau) {
        this.panneau = panneau;
    }

    public Secteur() throws Exception {
        super();
        this.setTable("secteur");
        this.setConnection("PostgreSQL");
        this.setPrimaryKeyName("id_secteur");
    }

    public int getNombreEtudiant(Date date, Time heure, Pointage pointage) throws Exception {
        int somme = 0;
        for (Salle s : this.getSalles()) {
            pointage.setSalle(s);
            somme += ((Pointage) pointage.getIntervalle(date, heure)).getNombre();
        }
        return somme;
    }

    public EtatSolaire getEtatSolaire(Date date, Meteo meteo, Pointage pointage, double consommation, int decallage) throws Exception {
        Time hours = Time.valueOf("08:00:00");
        EtatSolaire[] etatSolaires = new EtatSolaire[9 * decallage + 1];
        EtatSolaire etat = new EtatSolaire();
        double capacite = this.getPanneau().getCapacite();
        for (int i = 0; i < etatSolaires.length; i++) {
            // Luminosite par heure par date
            double luminosite = ((Meteo) meteo.getIntervalle(date, hours)).getLuminosite();
            
            // Nombre total du secteur
            int nombre = this.getNombreEtudiant(date, hours, pointage);

            etatSolaires[i] = new EtatSolaire(hours.toLocalTime(), luminosite, nombre, consommation, capacite, this);
            capacite -= etatSolaires[i].getReste(); // Capacite restante de la batterie 
            hours = Time.valueOf(hours.toLocalTime().plusMinutes(60 / decallage)); // Ajouter une heure
        }
        etat.setConsommation(consommation);
        etat.setDetails(etatSolaires);
        return etat;
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = BddObject.getPostgreSQL()) {
            Secteur secteur = new Secteur();
            secteur.setId("SEC001");
            Panneau panneau = new Panneau();
            panneau.setCapacite(40000);
            panneau.setPuissance(15000);
            secteur.setPanneau(panneau);
            Salle salle = new Salle();
            salle.setSecteur(secteur);
            Salle[] salles = (Salle[]) salle.findAll(connection, null);
            Date date = Date.valueOf("2023-11-27");
            secteur.setSalles(salles);
            Meteo meteo = Meteo.createMeteo(connection);
            Pointage pointage = Pointage.createPointage(connection);
            EtatSolaire etat = secteur.getEtatSolaire(date, meteo, pointage, 64, 1);
            System.out.println(etat.getHeureCoupure());
            EtatSolaire[] details = etat.getDetails();
            for (EtatSolaire etatSolaire : details) {
                System.out.println(etatSolaire.getHeure() + " " + etatSolaire.getConsommationEtudiant() + " " + etatSolaire.getPuissanceSolaire() + " " + etatSolaire.getReste() + " " + etatSolaire.getCapacite() + " " + etatSolaire.isCoupure());
            }
        }
    }
    
}