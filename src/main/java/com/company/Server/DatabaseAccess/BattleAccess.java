package com.company.Server.DatabaseAccess;

import com.company.Server.models.Response;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BattleAccess extends DBAccess {
    public BattleAccess() throws SQLException {
    }

    //Checkt ob es beitretbare Battles gibt
    public boolean battleAvaliable(String token) {
        try {
            PreparedStatement joinBattle = connection.prepareStatement(
                    "SELECT * FROM battle " +
                            "WHERE fk_player2 IS NULL"
            );
            ResultSet rs = joinBattle.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1) + "|" +
                        rs.getString(2) + "|" +
                        rs.getString(3) + "|" +
                        rs.getString(4)
                );
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Schaut nach ob ein Spieler bereits ein Battle gestartet hat, dem noch jemand beitreten kann
    public boolean hasOpenBattles(String token) {
        try {
            PreparedStatement joinBattle = connection.prepareStatement(
                    "SELECT * FROM battle " +
                            "INNER JOIN \"user\" u on u.id = battle.fk_player1 " +
                            "WHERE u.token = ? AND fk_player1 = u.id AND fk_player2 IS NULL"
            );
            joinBattle.setString(1, token);
            ResultSet rs = joinBattle.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1) + "|" +
                        rs.getString(2) + "|" +
                        rs.getString(3) + "|" +
                        rs.getString(4)
                );
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Erstellt ein Battle
    public Response create(String token) {
        try {
            String userId = new UserAccess().getId(token);
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO battle (id, fk_player1, fk_player2, fk_winner) VALUES (?,?,?,?)"
            );
            create.setString(1, UUID.randomUUID().toString());
            create.setString(2, userId);
            create.setString(3, null);
            create.setString(4, null);
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Battle konnte nicht erstellt werden\" }");
        }
        return new Response(200, "{ \"message\" : \"Battle erfolgreich erstellt\"}");
    }

    //Tritt einem Battle bei
    public Response join(String token) {
        //Checkt, ob der Spieler bereits ein offenes Spiel hat
        //Verhindert einen Beitritt zu seinem eigenen Battle
        if (hasOpenBattles(token)) {
            return new Response(400, "{ \"message\" : \"Warten auf andere Spieler...\" }");
        }
        //Wenn der Spieler noch kein offenes Spiel hat, dann tritt er einem Spiel bei
        try {
            String userId = new UserAccess().getId(token);
            PreparedStatement create = connection.prepareStatement(
                    "UPDATE battle SET fk_player2 = ? WHERE fk_player2 IS NULL"
            );
            create.setString(1, userId);
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Battle konnte nicht beigetreten werden\" }");
        }
        return new Response(200, "{ \"message\" : \"Battle erfolgreich beigetreten\"}");
    }


}
