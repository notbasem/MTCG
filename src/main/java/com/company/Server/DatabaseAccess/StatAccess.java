package com.company.Server.DatabaseAccess;

import com.company.Server.models.Response;
import com.company.Server.models.Scoreboard;
import com.company.Server.models.Stat;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatAccess extends DBAccess{
    public StatAccess() throws SQLException {
    }

    public void create(Stat stat) throws SQLException {
        System.out.println(stat.getUserId());
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO mtcg.public.stat (id, fk_user) VALUES (?,?)"
            );
            create.setString(1, stat.getId());
            create.setString(2, stat.getUserId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public Response read(String token) throws SQLException {
        Stat stat = new Stat(null);
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM stat INNER JOIN \"user\" u on u.id = stat.fk_user " +
                            "WHERE u.token = ?"
            );
            read.setString(1, token);
            ResultSet rs = read.executeQuery();

            if (rs.next()) {
                stat = new Stat(rs.getString(1), rs.getInt(2),
                        rs.getInt(6), rs.getInt(3), rs.getInt(4),
                        rs.getInt(7), rs.getString(5));
            }

            if (stat.getUserId() == null) {
                return new Response(400, "{ \"message\": \"Stats could not be read\" }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"Stats could not be read\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\": \"Stats read successfully\", " +
                "\"stats\": " + stat +" }");
    }

    public Response scoreboard() throws SQLException {

        List<Scoreboard> scoreboard = new ArrayList<>();
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT stat.id, elo, total, wins, defeats, draws, fk_user, username, token, coins, name, bio, image FROM stat " +
                            "LEFT JOIN \"user\" u on u.id = stat.fk_user " +
                            "ORDER BY elo DESC"
            );
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                User user = new User(rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getInt(10), rs.getString(11),
                        rs.getString(12), rs.getString(13)
                );
                Stat stat = new Stat(rs.getString(1), rs.getInt(2),
                        rs.getInt(3), rs.getInt(4), rs.getInt(5),
                        rs.getInt(6), rs.getString(7));

                scoreboard.add(new Scoreboard(user, stat));
            }

            if (scoreboard.isEmpty()) {
                return new Response(400, "{ \"message\": \"Scoreboard could not be read\" }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"Scoreboard could not be read\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\": \"Scoreboard read successfully\", " +
                "\"stats\": " + scoreboard +" }");
    }

    public void updateWinner(String winner) throws SQLException {
        try {
            PreparedStatement updateWinner = connection.prepareStatement(
                    "UPDATE stat SET elo = elo + 3, wins = wins + 1, total = total + 1 WHERE fk_user = ?"
            );
            updateWinner.setString(1, winner);
            updateWinner.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public void updateLoser(String loser) throws SQLException {
        try {
            PreparedStatement updateLoser = connection.prepareStatement(
                    "UPDATE stat SET elo = elo -5, defeats = defeats + 1, total = total + 1 WHERE fk_user = ?"
            );
            updateLoser.setString(1, loser);
            updateLoser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public void updateDraw(String player1, String player2) throws SQLException {
        try {
            PreparedStatement updateWinner = connection.prepareStatement(
                    "UPDATE stat SET total = total + 1, draws = draws + 1  WHERE fk_user = ? OR fk_user = ?"
            );
            updateWinner.setString(1, player1);
            updateWinner.setString(2, player2);
            updateWinner.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}
