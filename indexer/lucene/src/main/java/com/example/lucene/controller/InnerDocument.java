package com.example.lucene.controller;

public class InnerDocument {
    private String title;
    private String url;
    private String description;

    // Constructor
    public InnerDocument(String title, String url, String description) {
        this.title = title;
        this.url = url;
        this.description = description;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for url
    public String getUrl() {
        return url;
    }

    // Setter for url
    public void setUrl(String url) {
        this.url = url;
    }

    // Getter for description
    public String getDescription() {
        return description;
    }

    // Setter for description
    public void setDescription(String description) {
        this.description = description;
    }
}

