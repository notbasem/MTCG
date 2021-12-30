package com.company.Server.models;

public class Scoreboard {
    private User user;
    private Stat stat;

    public Scoreboard(User user, Stat stat) {
        this.user = user;
        this.stat = stat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        return "{" +
                "\"username\": \"" + user.getUsername() + "\", " +
                "\"stat\": " + stat.toString() +
                "}";
    }
}
