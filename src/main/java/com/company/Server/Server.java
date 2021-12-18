package com.company.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            System.out.println("Server started on 127.0.0.1:10001");
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    new ClientHandler(client);
                }
            }
        }
    }
}

