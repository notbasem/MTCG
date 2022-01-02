package com.company.Server.DatabaseAccess;

import com.company.Server.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class BattleAccess extends DBAccess {
    public BattleAccess() throws SQLException {
    }

    public Battle createOrJoin(String token) {
        Battle battle = null;
        try {
            PreparedStatement createOrJoin = connection.prepareStatement(
                    "SELECT * FROM battle WHERE fk_player2 IS NULL LIMIT 1"
            );
            ResultSet rs = createOrJoin.executeQuery();

            if (rs.next()) {
                battle = getBattle(rs.getString(1));
            } else {
                battle = createBattle();
            }

            battle = addPlayer(battle, token);
            if (battle == null) {
                System.out.println("Bereits ein offenes Battle vorhanden");
                return null;
            }

            System.out.println(battle);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    private Battle createBattle () {
        Battle battle = null;
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO battle (id, fk_player1, fk_player2, fk_winner) VALUES (?,?,?,?)"
            );
            String id = UUID.randomUUID().toString();
            create.setString(1, id);
            create.setString(2, null);
            create.setString(3, null);
            create.setString(4, null);
            create.execute();

            battle = new Battle(id, null, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    private Battle addPlayer (Battle battle, String token) {
        try {
            String userId = new UserAccess().getId(token);
            PreparedStatement create;
            if (battle.getPlayer1() == null) {
                System.out.println("PLAYER1");
                create = connection.prepareStatement(
                        "UPDATE battle SET fk_player1 = ? WHERE id = ?"
                );
                battle.setPlayer1(userId);
            } else if (battle.getPlayer2() == null && !battle.getPlayer1().equals(userId)){
                System.out.println("PLAYER2");
                create = connection.prepareStatement(
                        "UPDATE battle SET fk_player2 = ? WHERE id = ?"
                );
                battle.setPlayer2(userId);
            } else {
                System.out.println("Bereits ein offenes Battle vorhanden");
                return null;
            }
            create.setString(1, userId);
            create.setString(2, battle.getId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return battle;
    }

    private Battle getBattle (String battleId) {
        System.out.println(battleId);
        Battle battle = null;
        try {
            PreparedStatement create = connection.prepareStatement(
                    "SELECT * FROM battle WHERE id = ?"
            );
            create.setString(1, battleId);
            ResultSet rs = create.executeQuery();

            if (rs.next()) {
                battle = new Battle(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    public Response battle(Battle battle) throws SQLException {
        if (battle == null) {
            return new Response(400, "{ \"message\" : \"Bereits ein offenes Battle vorhanden\"}");
        } else if (battle.getPlayer1() == null || battle.getPlayer2() == null) {
            return new Response(200, "{ \"message\" : \"Warten auf Spieler...\"}");
        }
        UserAccess userAccess = new UserAccess();
        User player1 = userAccess.getUser(battle.getPlayer1());
        User player2 = userAccess.getUser(battle.getPlayer2());
        System.out.println("PLAYER 1: " + player1);
        System.out.println("PLAYER 2: " + player2);

        DeckAccess deckAccess = new DeckAccess();
        Deck deck1 = deckAccess.getDeck(battle.getPlayer1());
        Deck deck2 = deckAccess.getDeck(battle.getPlayer2());

        System.out.println("Deck1: " + deck1);
        System.out.println("Deck1: " + deck2);


        //Wenn der Spieler noch kein offenes Spiel hat, dann tritt er einem Spiel bei
        return new Response(200, "{ \"message\" : \"Battle erfolgreich abgeschlossen\"}");
    }

    private void newRound() {
        try {
            PreparedStatement create = connection.prepareStatement(
                    "SELECT * FROM round"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




}
