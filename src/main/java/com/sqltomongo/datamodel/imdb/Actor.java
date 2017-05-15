package com.sqltomongo.datamodel.imdb;

import java.util.List;

public class Actor {

    private String nom;
    private String prenom;
    private String genre;
    private String date_born;
    List<String> movies;

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

    public String getGenre() {
        return genre;
    }

    public Actor setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public String getDate_born() {
        return date_born;
    }

    public Actor setDate_born(String date_born) {
        this.date_born = date_born;
        return this;
    }

    public List<String> getMovies() {
        return movies;
    }

    public Actor setMovies(List<String> movies) {
        this.movies = movies;
        return this;
    }
}
