package model.pointage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import model.secteur.Salle;
import model.temps.Intervalle;

public class Pointage extends Intervalle {

    Salle salle;
    double nombre;

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public double getNombre() {
        return nombre;
    }

    public void setNombre(double nombre) {
        this.nombre = nombre;
    }

    public Pointage() throws Exception {
        super();
        this.getColumn("date").setName("date_pointage");
        this.setTable("pointage");
        this.setPrimaryKeyName("id_pointage");
        this.setConnection("PostgreSQL");
    }

    public static Pointage createPointage(Connection connection) throws Exception {
        return (Pointage) createIntervalle(connection, new Pointage());
    }

    public Pointage(Date date, Time debut, Time fin, Salle salle, double nombre) throws Exception {
        this();
        this.setDate(date);
        this.setDebut(debut);
        this.setFin(fin);
        this.setSalle(salle);
        this.setNombre(nombre);
    }
    
}