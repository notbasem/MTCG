package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.BattleAccess;
import com.company.Server.DatabaseAccess.DeckAccess;
import com.company.Server.models.Response;

import java.io.IOException;
import java.sql.SQLException;

public class BattleController {
    public void handle(ClientHandler client) throws SQLException, IOException {
        boolean battleAvaliable = new BattleAccess().battleAvaliable(client.getToken().replaceAll("Basic ", ""));
        if (battleAvaliable) {
            join(client);
        } else {
            create(client);
        }
    }

    private void create(ClientHandler client) throws IOException {
        //Random Deck auslesen
        Response response = null;
        try {
            BattleAccess battleAccess = new BattleAccess();
            response = battleAccess.create(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }

    private void join(ClientHandler client) throws IOException {
        //Random Deck auslesen
        Response response = null;
        try {
            BattleAccess battleAccess = new BattleAccess();
            response = battleAccess.join(client.getToken().replaceAll("Basic ", ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.sendResponse(client);
    }
}
