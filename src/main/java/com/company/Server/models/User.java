package com.company.Server.models;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public class User {
    private String id;
    private String username;
    private String password;

    @JsonCreator
    public User(@JsonProperty("username") String username, @JsonProperty("password") String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.id = UUID.randomUUID().toString();
        this.username = username.toLowerCase();
        this.password = password;
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

    public String getPassword() {
        return password;
    }


}
