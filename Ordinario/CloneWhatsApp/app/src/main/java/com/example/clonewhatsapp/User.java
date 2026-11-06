package com.example.clonewhatsapp;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String username;
    private String fullName;
    private String imgUrl;

    public User() {
    }

    public User(String id, String username, String fullName, String imgUrl) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.imgUrl = imgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (username != null && !username.trim().isEmpty()) {
            return username;
        }
        return "Usuario sin nombre";
    }
}
