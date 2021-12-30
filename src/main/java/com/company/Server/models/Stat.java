package com.company.Server.models;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Stat {
    private String id;
    private int elo;
    private int wins;
    private int defeats;
    String userId;

    public Stat(String userId) {
        this.id = UUID.randomUUID().toString();
        this.elo = 100;
        this.wins = 0;
        this.defeats = 0;
        this.userId = userId;
    }

    public Stat(String id, int elo, int wins, int defeats, String userId) {
        this.id = id;
        this.elo = elo;
        this.wins = wins;
        this.defeats = defeats;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\", " +
                "\"elo\": \"" + elo + "\"," +
                "\"wins\": \"" + wins + "\"," +
                "\"defeats\": \"" + defeats + "\"," +
                "\"userId\": \"" + userId + "\" " +
                "}";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDefeats() {
        return defeats;
    }

    public void setDefeats(int defeats) {
        this.defeats = defeats;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
