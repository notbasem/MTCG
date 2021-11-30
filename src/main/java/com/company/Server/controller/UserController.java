package com.company.Server.controller;

import com.company.Server.DatabaseAccess.UserAccess;
import com.company.Server.models.Response;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

public class UserController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equals("POST")) {
            //Request in String umwandeln
            String json = requestToJSON(exchange);

            //Registrierung, wenn POST-Request auf /api/users gemacht wird
            if(exchange.getRequestURI().toString().equalsIgnoreCase("/api/users")) {
                //CREATE-Methode aufrufen und Response an den User liefern
                create(exchange, json);
            }
            //Login, wenn API-Request auf /api/sessions gemacht wird
            else if(exchange.getRequestURI().toString().equalsIgnoreCase("/api/sessions")) {
                //Login-Methode aufrufen und Response an den User liefern
                login(exchange, json);
            }
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    /**
     * CREATE-METHODE
     * @param exchange
     * @param json
     * @return
     * @throws JsonProcessingException
     */

    private void create(HttpExchange exchange, String json) throws IOException {
        //ObjectMapper erstellen, der im JSON nicht auf Groß/Kleinschreibung achtet
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(json, User.class);

        //User in die Datenbank eintragen
        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.createUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(exchange);
    }

    /**
     * LOGIN-METHODE
     * @param exchange
     * @param json
     * @return
     * @throws JsonProcessingException
     */
    private void login(HttpExchange exchange, String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(json, User.class);

        Response response = null;
        try {
            UserAccess userAccess = new UserAccess();
            response = userAccess.loginUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(exchange);
    }

    private String requestToJSON(HttpExchange exchange) {
        String json = (new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining("\r\n")));
        return json;
    }
}
