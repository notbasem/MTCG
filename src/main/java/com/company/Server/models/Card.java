package com.company.Server.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    private String id;
    private String name;
    private float damage;
    private String type;

    @JsonCreator
    public Card(@JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("damage") float damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        if (name.contains("Spell")) {
            this.type = "Spell";
        } else {
            this.type = "Monster";
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /*
    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", type='" + type + '\'' +
                '}';
    }
    */

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\"," +
                "\"name\": \"" + name + "\"," +
                "\"damage\": \"" + damage + "\"," +
                "\"type\": \"" + type + "\"" +
                "}";
    }
}
