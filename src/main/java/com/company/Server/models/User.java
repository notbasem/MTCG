package com.company.Server.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String password;
    private String token;
    private int coins;

    @JsonCreator
    public User(@JsonProperty("username") String username, @JsonProperty("password") String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.id = UUID.randomUUID().toString();
        this.username = username.toLowerCase();
        this.password = password;
        this.token = this.username+"-mtcgToken";
        this.coins = 20;
    }

    public User(String id, String username, String token, int coins) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.token = token;
        this.coins = coins;
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
}
