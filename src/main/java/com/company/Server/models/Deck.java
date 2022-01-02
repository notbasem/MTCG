package com.company.Server.models;

import java.util.List;

public class Deck {
    private String id;
    private List<Card> cards;

    public Deck(String id, List<Card> cards) {
        this.id = id;
        this.cards = cards;
    }

    public Deck(String id) {
        this.id = id;
        this.cards = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
