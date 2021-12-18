package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.UserAccess;
import com.company.Server.models.Response;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.sql.*;

public class UserController {
    public void handle(ClientHandler client) throws IOException {
        if(client.getMethod().equals("POST")) {
            //Request in String umwandeln
            System.out.println(client.getBody());

            //Registrierung, wenn POST-Request auf /api/users gemacht wird
            if(client.getUri().toString().equalsIgnoreCase("/api/users")) {
                //CREATE-Methode aufrufen und Response an den User liefern
                create(client);
            }
            //Login, wenn API-Request auf /api/sessions gemacht wird
            else if(client.getUri().toString().equalsIgnoreCase("/api/sessions")) {
                //Login-Methode aufrufen und Response an den User liefern
                login(client);
            }
        } else {
            new Response(405, null).sendResponseHeaders(client);// 405 Method Not Allowed
        }
    }

    /**
     * CREATE-METHODE
     * @param client
     * @return
     * @throws JsonProcessingException
     */

    private void create(ClientHandler client) throws IOException {
        //ObjectMapper erstellen, der im JSON nicht auf Groß/Kleinschreibung achtet
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(client.getBody(), User.class);

        //User in die Datenbank eintragen
        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.createUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    /**
     * LOGIN-METHODE
     * @param client
     * @return
     * @throws JsonProcessingException
     */
    private void login(ClientHandler client) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(client.getBody(), User.class);

        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.loginUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
