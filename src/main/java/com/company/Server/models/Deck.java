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

    public String soutCards() {
        String erg = "";
        for (int i=0; i<cards.size()-1; i++) {
            erg += cards.get(i).getName() + "(" + cards.get(i).getDamage() +") | ";
        }
        erg += cards.get(cards.size()-1).getName() + "(" + cards.get(cards.size()-1).getDamage() +"), ";

        return erg;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\"," +
                "\"Cards\": \"" + cards + "\"" +
                "}";
    }
}
