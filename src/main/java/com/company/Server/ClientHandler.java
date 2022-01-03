package com.company.Server;

import com.company.Server.controller.*;
import com.company.Server.models.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ClientHandler {
    private Socket client;
    private String method;
    private String uri;
    private String version;
    private String host;
    private int contentLength;
    private List<String> headers;
    private String body = "";

    public ClientHandler(Socket client) throws IOException, SQLException {
        this.client = client;

        BufferedReader br = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            System.out.println(line);
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        this.method = requestLine[0];
        this.uri = requestLine[1];
        this.version = requestLine[2];
        this.host = requestsLines[1].split(" ")[1];
        this.headers = Arrays.stream(requestsLines).skip(2).collect(Collectors.toList());
        System.out.println(headers);

        //Mittels Java Streams schauen ob Content-Length vorhanden ist und initialisieren
        if(headers.stream().anyMatch(x -> x.contains("Content-Length"))) {
            this.contentLength = Integer.parseInt(String.valueOf(headers.stream()
                            .filter(x -> x.contains("Content-Length"))
                            .findFirst())
                            .replaceAll("\\D", ""));

            System.out.println("CONTENT-LENGTH: " + this.contentLength);

            if (contentLength > 0) {
                //Body auslesen und in this.body speichern
                int read;
                while ((read = br.read()) != -1) {
                    this.body += (char) read;
                    if (this.body.length() == this.contentLength) {
                        break;
                    }
                }
            }
            System.out.println(body);
        }

        //routing() aufrufen, um zu den entsprechenden Controllern zu gelangen
        routing();
    }

    // TODO: hasAuthheader & verifyUser() besser einsetzen und dadurch if-abfragen geringer machen
    private void routing() throws IOException, SQLException {
        if (this.getUri().equals("/users") && this.getMethod().equals("POST")) {
            new UserController().create(this);
        } else if (this.getUri().matches("/users/\\w+") && this.getMethod().equals("GET")) {
            if (verifyUser(this.createUsertokenFromURL())) {
                new UserController().read(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().matches("/users/\\w+") && this.getMethod().equals("PUT")) {
            if (verifyUser(this.createUsertokenFromURL())) {
                new UserController().update(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        }
        else if (this.getUri().equals("/sessions") && this.getMethod().equals("POST")) {
            new UserController().login(this);
        } else if (this.getUri().equals("/packages") && this.getMethod().equals("POST")) {
            if (this.verifyUser("Basic admin-mtcgToken")) {
                new PackageController().create(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/packages") && this.getMethod().equals("GET")) {
            new PackageController().read(this);
        } else if (this.getUri().equals("/transactions/packages") && this.getMethod().equals("POST")) {
            if (hasAuthorizationHeader()) {
                new PackageController().acquire(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/cards") && this.getMethod().equals("GET")) {
            if (hasAuthorizationHeader()) {
                new PackageController().read(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/deck") && this.getMethod().equals("GET")) {
            if (hasAuthorizationHeader()) {
                new DeckController().read(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/deck") && this.getMethod().equals("PUT")) {
            if (hasAuthorizationHeader()) {
                new DeckController().configure(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/deck?format=plain") && this.getMethod().equals("GET")) {
            if (hasAuthorizationHeader()) {
                new DeckController().readPlain(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/stats") && this.getMethod().equals("GET")) {
            if (hasAuthorizationHeader()) {
                new StatController().read(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/score") && this.getMethod().equals("GET")) {
            if (hasAuthorizationHeader()) {
                new StatController().scoreboard(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (this.getUri().equals("/battles") && this.getMethod().equals("POST")) {
            if (hasAuthorizationHeader()) {
                new BattleController().battle(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else {
            new Response(405, "{ \"message\": \"Method not allowed\" }").sendResponse(this);
        }
    }

    private boolean hasAuthorizationHeader() {
        return this.headers.stream().anyMatch(x -> x.contains("Authorization"));
    }

    private boolean verifyUser(String expectedToken) {
        if (hasAuthorizationHeader()) {
            System.out.println("GIVEN-TOKEN: " + this.getToken());
            return this.getToken().equals(expectedToken);
        }

        return false;
    }

    //Usertoken aus dem Usernamen in der URL erstellen
    private String createUsertokenFromURL() {
        return "Basic " + this.getUri().replaceAll("/users/", "") + "-mtcgToken";
    }

    public String getToken() {
        if (!hasAuthorizationHeader()) {
            return null;
        }

        return headers.stream()
                .filter(x -> x.contains("Authorization"))
                .findFirst()
                .toString()
                .split(":")[1]
                .replaceFirst(" ", "")
                .replaceFirst("\\]", "");
    }

    public Socket getClient() {
        return client;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getBody() {
        return body;
    }
}
