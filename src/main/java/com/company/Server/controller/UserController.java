package com.company.Server.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
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
            InputStream res = exchange.getRequestBody();
            String json = (new BufferedReader(new InputStreamReader(res))
                    .lines().collect(Collectors.joining("\r\n")));

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

    public String create(HttpExchange exchange, String json) throws JsonProcessingException {
        String respText = "";

        //ObjectMapper erstellen, der im JSON nicht auf Groß/Kleinschreibung achtet
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(json, User.class);

        //User in die Datenbank eintragen
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mtcg",
                    "basem",
                    "");

            //Überprüfen, ob der username bereits vorhanden ist
            PreparedStatement read = connection.prepareStatement(
                    "SELECT username FROM mtcg.public.user WHERE username = ?"
            );
            read.setString(1, user.getUsername());
            ResultSet rs = read.executeQuery();
            //Wenn username nicht vorhanden ist, User erstellen
            if (!rs.next()) {
                PreparedStatement create = connection.prepareStatement(
                        "INSERT INTO mtcg.public.user (id, username, password, token) " +
                                "VALUES (?,?,?, ?);"
                );

                //Password hash erstellen mit: https://github.com/patrickfav/bcrypt
                user.setPassword(BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray()));

                create.setString(1, user.getId());
                create.setString(2, user.getUsername());
                create.setString(3, user.getPassword());
                create.setString(4, user.getToken());
                create.executeUpdate();
                respText = "{ \"message\" : \"User erstellt\" }";
                sendResponse(exchange, 200, respText);

                System.out.println("Registrierung erfolgreich:");
                System.out.println("Username: " + user.getUsername() + " | Password: " + user.getPassword() + " | Token: " + user.getToken());
            } else {
                respText = "{ \"message\" : \"Username bereits vorhanden\" }";
                sendResponse(exchange, 409, respText);
                System.out.println("Registrierung fehlgeschlagen.");
            }
            rs.close();
            read.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return respText;
    }

    public String login(HttpExchange exchange, String json) throws JsonProcessingException {
        String respText = "";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        User user = objectMapper.readValue(json, User.class);

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mtcg",
                    "basem",
                    "");

            //Überprüfen, ob der username bereits vorhanden ist
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.user WHERE username = ?"
            );
            read.setString(1, user.getUsername());
            //Passwort hash des Users zum vergleichen holen
            ResultSet rs = read.executeQuery();
            String hash = null;
            while (rs.next()) {
                hash = rs.getString("password");
            }
            rs.close();
            read.close();

            //Passwörter miteinander vergleichen
            BCrypt.Result result = BCrypt.verifyer().verify(user.getPassword().toCharArray(), hash);

            if (result.verified) {
                String userJson = objectMapper.writeValueAsString(user);
                respText = "{ \"message\" : \"Login erfolgreich\", \"user\" : " + userJson + " }";
                sendResponse(exchange, 200, respText);
                System.out.println("Login erfolgreich");
            } else {
                respText = "{ \"message\" : \"Username und Passwort stimmen nicht überein\" }";
                sendResponse(exchange, 401, respText);
                System.out.println("Login fehlgeschlagen");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return respText;
    }

    private void sendResponse (HttpExchange exchange, int status, String respText) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, respText.getBytes().length);
        OutputStream output = exchange.getResponseBody();
        output.write(respText.getBytes());
        output.flush();
    }
}
