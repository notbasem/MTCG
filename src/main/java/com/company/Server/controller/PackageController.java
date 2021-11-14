package com.company.Server.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.company.Server.models.Card;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PackageController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equals("POST")) {
            //Request in String umwandeln
            InputStream res = exchange.getRequestBody();
            String json = (new BufferedReader(new InputStreamReader(res))
                    .lines().collect(Collectors.joining("\r\n")));

            create(exchange, json);
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    public void create(HttpExchange exchange, String json) throws IOException {
        String respText = null;
        //Delete all whitespace from JSON-String
        json = json.replaceAll("\\s*", "");
        System.out.println("JSON:\n" + json);
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        ArrayList<Card> cards = new ArrayList<>();

        //Split JSON into several objects
        while (matcher.find()) {
            Card card = objectMapper.readValue(matcher.group(0), Card.class);
            cards.add(card);

            Connection connection = null;
            try {
                connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/mtcg",
                        "basem",
                        "");

                //Create cards
                PreparedStatement create = connection.prepareStatement(
                        "INSERT INTO mtcg.public.card (id, name, damage) " +
                                "VALUES (?,?,?);"
                );

                create.setString(1, card.getId());
                create.setString(2, card.getName());
                create.setFloat(3, card.getDamage());

                create.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                respText = "{ \"message\" : \"Cards konnten nicht erstellt werden\" }";
                sendResponse(exchange, 400, respText);
                break;
            }
        }
        System.out.println(cards);
        respText = "{ \"message\" : \"Cards erfolgreich erstellt\" }";
        sendResponse(exchange, 200, respText);
    }

    private void sendResponse (HttpExchange exchange, int status, String respText) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, respText.getBytes().length);
        OutputStream output = exchange.getResponseBody();
        output.write(respText.getBytes());
        output.flush();
    }
}
