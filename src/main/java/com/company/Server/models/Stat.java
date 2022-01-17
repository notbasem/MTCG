package com.company.Server.models;

import java.beans.ConstructorProperties;
import java.util.UUID;

public class Stat {
    private String id;
    private int elo;
    private int total;
    private int wins;
    private int defeats;
    private int draws;
    String userId;

    public Stat(String userId) {
        this.id = UUID.randomUUID().toString();
        this.elo = 100;
        this.total = 0;
        this.wins = 0;
        this.defeats = 0;
        this.draws = 0;
        this.userId = userId;
    }

    public Stat(String id, int elo, int total, int wins, int defeats, int draws,  String userId) {
        this.id = id;
        this.elo = elo;
        this.total = total;
        this.wins = wins;
        this.defeats = defeats;
        this.draws = draws;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\", " +
                "\"elo\": \"" + elo + "\"," +
                "\"total\": \"" + total + "\"," +
                "\"wins\": \"" + wins + "\"," +
                "\"defeats\": \"" + defeats + "\"," +
                "\"draws\": \"" + draws + "\"," +
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }
}
