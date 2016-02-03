package com.example.ayman.pop;

/**
 * Created by Ayman on 19-Dec-15.
 */
public class GridItem {
    private String image;
    private Movie mov;

    public GridItem() {
        super();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Movie getMov() {
        return mov;
    }

    public void setMov(Movie mov) {
        this.mov = mov;
    }
}