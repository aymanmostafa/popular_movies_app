package com.example.ayman.pop;

import java.io.Serializable;

/**
 * Created by Ayman on 19-Dec-15.
 */

public class Movie implements Serializable {
    String poster_path, overview, release_date, original_title, offline_path;
    double vote_average;
    int id;

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getOffline_path() {
        return offline_path;
    }

    public void setOffline_path(String offline_path) {
        this.offline_path = offline_path;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Movie(String poster_path, String overview, String release_date, String original_title, String offline_path, double vote_average, int id) {
        this.poster_path = poster_path;
        this.overview = overview;
        this.release_date = release_date;
        this.original_title = original_title;
        this.offline_path = offline_path;
        this.vote_average = vote_average;
        this.id = id;
    }

    public Movie() {
    }
}
