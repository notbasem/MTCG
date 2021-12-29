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
    /**
     * CREATE-METHODE
     * @param client
     * @return
     * @throws JsonProcessingException
     */

    public void create(ClientHandler client) throws IOException {
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
    public void login(ClientHandler client) throws IOException {
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

    public void read(ClientHandler client) throws IOException {
        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.read(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    public void update(ClientHandler client) throws IOException {
        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.update(client.getBody(), client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
