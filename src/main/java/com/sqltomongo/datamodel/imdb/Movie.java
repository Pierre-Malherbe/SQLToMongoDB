package com.sqltomongo.datamodel.imdb;

import java.util.List;

public class Movie {

    private String title;
    private List<String> keywords;
    private List<Actor> actors;
    private List<String> types;
    private String director;
    private int year;

    public String getTitle() {
        return title;
    }

    public Movie setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Movie setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public Movie setActors(List<Actor> actors) {
        this.actors = actors;
        return this;
    }

    public List<String> getTypes() {
        return types;
    }

    public Movie setTypes(List<String> types) {
        this.types = types;
        return this;
    }

    public String getDirector() {
        return director;
    }

    public Movie setDirector(String director) {
        this.director = director;
        return this;
    }

    public int getYear() {
        return year;
    }

    public Movie setYear(int year) {
        this.year = year;
        return this;
    }
}
