package com.company.Server.controller;

import com.company.Server.ClientHandler;
import com.company.Server.DatabaseAccess.BattleAccess;
import com.company.Server.DatabaseAccess.DeckAccess;
import com.company.Server.models.Battle;
import com.company.Server.models.Response;

import java.io.IOException;
import java.sql.SQLException;

public class BattleController {
    public void battle(ClientHandler client) throws SQLException, IOException {
        BattleAccess battleAccess = new BattleAccess();
        String token = client.getToken().replaceAll("Basic ", "");
        Battle battle = battleAccess.createOrJoin(token);

        //Battle starten
        Response response = battleAccess.battle(battle);
        response.sendResponse(client);

    }
}
