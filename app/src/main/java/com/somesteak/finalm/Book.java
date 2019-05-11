package com.somesteak.finalm;

public class Book {

    private String title;
    private String author;
    private String latest;
    private String owned;
    private String image;
    public Book() {
        this.title = "";
        this.author = "";
        this.latest = "";
        this.owned = "";
        this.image = "";
    }

    public Book(String title, String author, String latest, String owned, String image) {
        this.title = title;
        this.author = author;
        this.latest = latest;
        this.owned = owned;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLatest() {
        return latest;
    }

    public void setLatest(String  latest) {
        this.latest = latest;
    }

    public String  getOwned() {
        return owned;
    }

    public void setOwned(String owned) {
        this.owned = owned;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
