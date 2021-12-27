package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.CardAccess;
import com.company.Server.models.Response;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.*;

public class PackageController {
    public void create(ClientHandler client) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        //User in die Datenbank eintragen
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.createCards(client.getBody(), objectMapper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendResponse(client);
    }

    public void read(ClientHandler client) throws IOException {
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.readCards(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    public void acquire(ClientHandler client) throws IOException {
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.acquire(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
