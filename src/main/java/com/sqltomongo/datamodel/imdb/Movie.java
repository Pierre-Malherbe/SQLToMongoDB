package com.sqltomongo.datamodel.imdb;

import java.util.List;

public class Movie {

    private String id;
    private String title;
    private String keywords;
    private List<Actor> actors;
    private String director;
    private String year;
    private String info;

    public String getId() {
        return id;
    }

    public Movie setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Movie setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getKeywords() {
        return keywords;
    }

    public Movie setKeywords(String keywords) {
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

    public String getDirector() {
        return director;
    }

    public Movie setDirector(String director) {
        this.director = director;
        return this;
    }

    public String getYear() {
        return year;
    }

    public Movie setYear(String year) {
        this.year = year;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public Movie setInfo(String info) {
        this.info = info;
        return this;
    }
}
