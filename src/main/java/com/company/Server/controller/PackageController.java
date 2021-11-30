package com.company.Server.controller;

import com.company.Server.DatabaseAccess.CardAccess;
import com.company.Server.DatabaseAccess.UserAccess;
import com.company.Server.models.Card;
import com.company.Server.models.Package;
import com.company.Server.models.Response;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PackageController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equals("POST")) {
            //Request in String umwandeln
            String json = requestToJSON(exchange);

            //Package mit Cards erstellen
            create(exchange, json);
        } else if (exchange.getRequestMethod().equals("GET")) { //READ unnötig, nur zum Test
            //Packages mit Cards auslesen
            String json = requestToJSON(exchange);

            //Cards auslesen
            read(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    private void create(HttpExchange exchange, String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        //User in die Datenbank eintragen
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.createCards(json, objectMapper);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendResponse(exchange);
    }

    private void read(HttpExchange exchange) throws IOException {
        Response response = null;
        try {
            CardAccess cardAccess = new CardAccess();
            response = cardAccess.readCards();
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
