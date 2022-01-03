package com.company.Server.models;

import java.util.UUID;

public class Round {
    private String id;
    private Card card1;
    private Card card2;
    private Card winner;
    private Battle battle;

    public Round(Card card1, Card card2, Card winner, Battle battle) {
        this.id = UUID.randomUUID().toString();
        this.card1 = card1;
        this.card2 = card2;
        this.winner = winner;
        this.battle = battle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Card getCard1() {
        return card1;
    }

    public void setCard1(Card card1) {
        this.card1 = card1;
    }

    public Card getCard2() {
        return card2;
    }

    public void setCard2(Card card2) {
        this.card2 = card2;
    }

    public Card getWinner() {
        return winner;
    }

    public void setWinner(Card winner) {
        this.winner = winner;
    }

    public Battle getBattle() {
        return battle;
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
    }
}
