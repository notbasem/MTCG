package com.company.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        this.serverSocket = new ServerSocket(this.port);
        System.out.println("Server started on 127.0.0.1:" + this.port);

        this.run();
    }

    private void run() {
        while (true) {
            try {
                Socket client = this.serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(client);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(10001)) {
            System.out.println("Server started on 127.0.0.1:10001");
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    ClientHandler clientHandler = new ClientHandler(client);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}

