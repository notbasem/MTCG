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

public class ClientHandler implements Runnable {
    private Socket client;
    private BufferedReader br;
    private String method;
    private String uri;
    private String version;
    private String host;
    private int contentLength;
    private List<String> headers;
    private String body = "";

    public ClientHandler(Socket client) throws IOException, SQLException {
        this.client = client;
        this.br = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread());
        try {
            StringBuilder requestBuilder = new StringBuilder();
            String line = this.br.readLine();
            if (line != null) {
                System.out.println(line);
                requestBuilder.append(line + "\r\n");

                while (!line.isEmpty()) {
                    line = this.br.readLine();
                    System.out.println(line);
                    requestBuilder.append(line + "\r\n");
                }

                String request = requestBuilder.toString();
                List<String> requestLines = List.of(request.split("\r\n"));
                String[] requestLine = requestLines.get(0).split(" ");
                this.method = requestLine[0];
                this.uri = requestLine[1];
                this.version = requestLine[2];
                this.host = requestLines.get(0).split(" ")[1];
                this.headers = requestLines.stream().skip(2).collect(Collectors.toList());
                System.out.println(headers);

                //Mittels Java Streams schauen ob Content-Length vorhanden ist und initialisieren
                if (headers.stream().anyMatch(x -> x.contains("Content-Length"))) {
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
        } catch(IOException | SQLException e){
            e.printStackTrace();
        }
    }

    private void routing() throws IOException, SQLException {
        if (requestEquals("/users", "POST")) {
            new UserController().create(this);
        } else if (requestMatches("/users/\\w+")) {
            if (verifyUser(this.createUsertokenFromURL()) && this.getMethod().equals("GET")) {
                new UserController().read(this);
            } else if (verifyUser(this.createUsertokenFromURL()) && this.getMethod().equals("PUT")) {
                new UserController().update(this);
            } else {
                new Response().sendNotAuthorized(this);
            }
        } else if (requestEquals("/sessions", "POST")) {
            new UserController().login(this);
        } else if (requestEquals("/packages", "POST")) {
            if (this.verifyUser("Basic admin-mtcgToken")) {
                new PackageController().create(this);
            } else {
                new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
            }
        } else if (requestEquals("/packages", "GET")) {
            new PackageController().read(this);
        } else if (hasAuthorizationHeader()) { //Requests die Authorization benötigen
            if (requestEquals("/transactions/packages", "POST")) {
                new PackageController().acquire(this);
            } else if (requestEquals("/cards", "GET")) {
                new PackageController().read(this);
            } else if (requestEquals("/deck", "GET")) {
                new DeckController().read(this);
            } else if (requestEquals("/deck", "PUT")) {
                new DeckController().configure(this);
            } else if (requestEquals("/deck?format=plain", "GET")) {
                new DeckController().readPlain(this);
            } else if (requestEquals("/stats", "GET")) {
                new StatController().read(this);
            } else if (requestEquals("/score","GET")) {
                new StatController().scoreboard(this);
            } else if (requestEquals("/battles","POST")) {
                new BattleController().battle(this);
            } else if (requestEquals("/tradings","GET")) {
                new TradeController().read(this);
            } else if (requestEquals("/tradings","POST")) {
                new TradeController().create(this);
            } else if (requestMatches("/tradings/[a-z-A-Z0-9\\-]*","DELETE")) {
                new TradeController().delete(this);
            } else if (requestMatches("/tradings/[a-z-A-Z0-9\\-]*","POST")) {
                new TradeController().trade(this);
            }
        } else if (!hasAuthorizationHeader()) {
            new Response(401, "{ \"message\": \"Not Authorized\" }").sendResponse(this);
        } else {
            new Response(405, "{ \"message\": \"Method not allowed\" }").sendResponse(this);
        }
    }

    private boolean requestMatches(String regex) {
        return (this.getUri().matches(regex));
    }

    private boolean requestMatches(String regex, String method) {
        return (this.getUri().matches(regex) && this.getMethod().equals(method));
    }

    private boolean requestEquals(String uri, String method) {
        return (this.getUri().equals(uri) && this.getMethod().equals(method));
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
