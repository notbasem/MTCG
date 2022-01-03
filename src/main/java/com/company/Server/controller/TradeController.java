package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.DeckAccess;
import com.company.Server.DatabaseAccess.TradeAccess;
import com.company.Server.models.Response;
import com.company.Server.models.Trade;
import com.company.Server.models.User;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;

public class TradeController {
    public void read(ClientHandler client) throws IOException {
        Response response = null;
        try {
            TradeAccess tradeAccess = new TradeAccess();
            response = tradeAccess.read(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    public void create(ClientHandler client) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        Trade trade = objectMapper.readValue(client.getBody(), Trade.class);

        Response response = null;
        try {
            TradeAccess tradeAccess = new TradeAccess();
            response = tradeAccess.create(trade, client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    public void trade(ClientHandler client) throws IOException {
        Response response = null;
        try {
            TradeAccess tradeAccess = new TradeAccess();
            String tradeId = client.getUri().replaceAll("/tradings/", "");
            String cardId = client.getBody().replaceAll("\"", "");

            response = tradeAccess.trade(tradeId, cardId, client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
