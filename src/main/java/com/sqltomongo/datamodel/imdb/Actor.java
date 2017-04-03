package com.sqltomongo.datamodel.imdb;

import java.util.Date;
import java.util.List;

public class Actor {

    private String nom;
    private String prenom;
    private Date date_born;
    private Date date_death;
    private List<Movie> movies;

    public String getNom() {
        return nom;
    }

    public Actor setNom(String nom) {
        this.nom = nom;
        return this;
    }

    public String getPrenom() {
        return prenom;
    }

    public Actor setPrenom(String prenom) {
        this.prenom = prenom;
        return this;
    }

    public Date getDate_born() {
        return date_born;
    }

    public Actor setDate_born(Date date_born) {
        this.date_born = date_born;
        return this;
    }

    public Date getDate_death() {
        return date_death;
    }

    public Actor setDate_death(Date date_death) {
        this.date_death = date_death;
        return this;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public Actor setMovies(List<Movie> movies) {
        this.movies = movies;
        return this;
    }
}
