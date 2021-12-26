package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.CardAccess;
import com.company.Server.models.Response;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.*;

public class PackageController {
    public void handle(ClientHandler client) throws IOException {
        if(client.getMethod().equals("POST")) {
            //Request in String umwandeln
            System.out.println(client.getBody());

            //Package mit Cards erstellen
            create(client);
        } else if (client.getMethod().equals("GET")) { //READ unnötig, nur zum Test
            //Cards auslesen
            read(client);
        } else {
            new Response(405, null).sendResponseHeaders(client);// 405 Method Not Allowed
        }
    }

    private void create(ClientHandler client) throws IOException {
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

    private void read(ClientHandler client) throws IOException {
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.readCards();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
