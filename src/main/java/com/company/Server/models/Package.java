package com.company.Server.models;

import java.util.ArrayList;
import java.util.UUID;

public class Package {
    private String id;
    private ArrayList<Card> cards = null;
    private String fk_user;

    public Package(ArrayList<Card> cards) {
        this.id = UUID.randomUUID().toString();
        this.cards = cards;
        this.fk_user = null;
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

    public String getFk_user() {
        return fk_user;
    }

    public void setFk_user(String fk_user) {
        this.fk_user = fk_user;
    }
}
