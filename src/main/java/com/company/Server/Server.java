package com.company.Server;

import com.company.Server.controller.UserController;
import com.company.Server.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

import static java.lang.module.ModuleDescriptor.read;

public class Server {
    public static void main(String[] args) throws IOException {
        //Serverport
        HttpServer server = HttpServer.create(new InetSocketAddress(10001), 0);
        server.createContext("/api/users", new UserController());
        server.createContext("/api/sessions", new UserController());
        server.start();
        System.out.println("Server started, listening on 127.0.0.1:10001");
    }
}

