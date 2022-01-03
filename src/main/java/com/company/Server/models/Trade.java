package com.company.Server.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.beans.ConstructorProperties;

public class Trade {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String id;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String cardToTrade;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String type;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private float minimumDamage;
    private String userId;

    @ConstructorProperties({"id","cardToTrade", "type", "minimumDamage"})
    public Trade(String id, String cardToTrade, String type, float minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type.replaceFirst("\\w", (type.charAt(0)+"").toUpperCase());
        this.minimumDamage = minimumDamage;
        this.userId = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardToTrade() {
        return cardToTrade;
    }

    public void setCardToTrade(String cardToTrade) {
        this.cardToTrade = cardToTrade;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.replaceFirst("\\w", (type.charAt(0)+"").toUpperCase());
    }

    public float getMinimumDamage() {
        return minimumDamage;
    }

    public void setMinimumDamage(float minimumDamage) {
        this.minimumDamage = minimumDamage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": \"" + id + "\"," +
                "\"cardToTrade\": \"" + cardToTrade + "\"," +
                "\"type\": \"" + type + "\"," +
                "\"minimumDamage\": \"" + minimumDamage + "\"," +
                "\"userId\": \"" + userId + "\"" +
                "}";
    }
}
