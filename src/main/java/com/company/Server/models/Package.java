package com.company.Server.models;

import java.util.ArrayList;
import java.util.UUID;

public class Package {
    private String id;
    private ArrayList<Card> cards = null;

    public Package(ArrayList<Card> cards) {
        this.id = UUID.randomUUID().toString();
        this.cards = cards;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }
}
