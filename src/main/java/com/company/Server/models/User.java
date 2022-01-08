package com.company.Server.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.annotation.*;

import java.beans.ConstructorProperties;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String username;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    @JsonIgnore
    private String password;
    private String token;
    private int coins;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String name;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String bio;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String image;

    @ConstructorProperties({"username","password"})
    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username.toLowerCase();
        this.password = password;
        this.token = this.username+"-mtcgToken";
        this.coins = 20;
        this.name = null;
        this.bio = null;
        this.image = null;
    }

    public User(String id, String username, String token, int coins, String name, String bio, String image) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.token = token;
        this.coins = coins;
        this.name = name;
        this.bio = bio;
        this.image = image;
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

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\"," +
                "\"username\": \"" + username + "\"," +
                "\"token\": \"" + token + "\"," +
                "\"coins\": \"" + coins + "\"," +
                "\"name\": \"" + name + "\"," +
                "\"bio\": \"" + bio + "\"," +
                "\"image\": \"" + image + "\"" +
                "}";
    }
}
