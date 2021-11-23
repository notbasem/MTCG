package com.company.Server.controller;

import com.company.Server.models.Card;
import com.company.Server.models.Package;
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
            InputStream res = exchange.getRequestBody();
            String json = (new BufferedReader(new InputStreamReader(res))
                    .lines().collect(Collectors.joining("\r\n")));

            create(exchange, json);
        } else if (exchange.getRequestMethod().equals("GET")) { //READ unnötig, nur zum Test
            InputStream res = exchange.getRequestBody();
            String json = (new BufferedReader(new InputStreamReader(res))
                    .lines().collect(Collectors.joining("\r\n")));

            read(exchange, json);
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    private void create(HttpExchange exchange, String json) throws IOException {
        String respText = null;
        //Delete all whitespace from JSON-String
        json = json.replaceAll("\\s*", "");
        System.out.println("JSON:\n" + json);
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(json);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        ArrayList<Card> cards = new ArrayList<>();

        /**
         * Packages werden auch erstellt, wenn Cards nicht erstellt werden können
         * Dadurch gibt es auch Packages, die keine Karten beinhalten.
         * TODO: Nur Package erstellen, wenn auch Cards erstellt werden.
         */
        Package pack = new Package(cards);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mtcg",
                    "basem",
                    "");

            //Package erstellen
            PreparedStatement createPackage = connection.prepareStatement(
                    "INSERT INTO mtcg.public.package (id) " +
                            "VALUES (?);"
            );
            createPackage.setString(1, pack.getId());
            createPackage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //Split JSON into several objects to create several cards
        while (matcher.find()) {
            Card card = objectMapper.readValue(matcher.group(0), Card.class);
            cards.add(card);

            try {
                //Create cards
                PreparedStatement create = connection.prepareStatement(
                        "INSERT INTO mtcg.public.card (id, name, damage, card_package_id_fk) " +
                                "VALUES (?,?,?,?);"
                );

                create.setString(1, card.getId());
                create.setString(2, card.getName());
                create.setFloat(3, card.getDamage());
                create.setString(4, pack.getId());

                create.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                respText = "{ \"message\" : \"Cards konnten nicht erstellt werden\" }";
                sendResponse(exchange, 400, respText);
                break;
            }
        }
        pack.setCards(cards);
        System.out.println(pack.getCards());

        respText = "{ \"message\" : \"Cards erfolgreich erstellt\" }";
        sendResponse(exchange, 200, respText);
    }

    private void read(HttpExchange exchange, String json) throws IOException {
        String respText;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mtcg",
                    "basem",
                    "");

            //Package erstellen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card "
            );
            ResultSet rs = read.executeQuery();
            ArrayList<Card> cards = new ArrayList<>();

            while (rs.next()) {
                cards.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            System.out.println(cards);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
