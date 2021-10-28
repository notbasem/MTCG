package com.company.Server.controller;

import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.util.stream.Collectors;

public class UserController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equals("POST")) {
            //Request in String umwandeln
            InputStream res = exchange.getRequestBody();

            String json = (new BufferedReader(new InputStreamReader(res))
                    .lines().collect(Collectors.joining("\r\n")));

            //CREATE-Methode aufrufen
            create(json);

            //Response an den Client schicken
            String respText = "{ \"status\" : \"200\" }";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
        } else {
            exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
    }

    public void create(String json) throws JsonProcessingException {
        System.out.println("Create-Methode-JSON: \n" + json);
        //ObjectMapper erstellen, der im JSON nicht auf Groß/Kleinschreibung achtet
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        String json2 = "{ \"username\" : \"username\", \"password\" : \"user1\" }";
        User user = objectMapper.readValue(json, User.class);
        System.out.println("----------------------------");
        System.out.println("USER: "+ user.getUsername() + "; Password: " + user.getPassword());
        System.out.println("----------------------------");
    }
}
