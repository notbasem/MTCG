package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.StatAccess;
import com.company.Server.DatabaseAccess.UserAccess;
import com.company.Server.models.Response;

import java.io.IOException;
import java.sql.SQLException;

public class StatController {
    public void read(ClientHandler client) throws IOException {
        Response response = null;
        try {
            StatAccess statAccess = new StatAccess();
            response = statAccess.read(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    public void scoreboard(ClientHandler client) throws IOException {
        Response response = null;
        try {
            StatAccess statAccess = new StatAccess();
            response = statAccess.scoreboard();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
