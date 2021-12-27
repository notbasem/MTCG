package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.DeckAccess;
import com.company.Server.DatabaseAccess.UserAccess;
import com.company.Server.models.Deck;
import com.company.Server.models.Response;
import com.company.Server.models.User;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;

public class DeckController {
    public void read(ClientHandler client) throws IOException {
        //Random Deck auslesen
        Response response = null;
        try {
            DeckAccess deckAccess = new DeckAccess();
            response = deckAccess.read(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
