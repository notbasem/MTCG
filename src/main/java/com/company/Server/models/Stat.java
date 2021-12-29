package com.company.Server.models;

import java.util.UUID;

public class Stat {
    private String id;
    private int elo;
    private int wins;
    private int defeats;
    User user;

    public Stat(User user) {
        this.id = UUID.randomUUID().toString();
        this.elo = 100;
        this.wins = 0;
        this.defeats = 0;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
