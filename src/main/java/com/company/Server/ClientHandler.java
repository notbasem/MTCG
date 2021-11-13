package com.company.Server;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedWriter;
import java.net.Socket;

public class ClientHandler {
    HttpServer server;

    public ClientHandler(HttpServer server) {
        this.server = server;
    }
}
