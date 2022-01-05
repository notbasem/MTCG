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
                        rs.getInt(3), rs.getInt(4), rs.getString(5));
            }

            if (stat.getUserId() == null) {
                return new Response(400, "{ \"message\": \"Stats konnten nicht ausgelesen werden\" }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"Stats konnten nicht ausgelesen werden\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\": \"Stats konnten erfolgreich ausgelesen werden\", " +
                "\"stats\": " + stat +" }");
    }

    public Response scoreboard(String token) throws SQLException {

        List<Scoreboard> scoreboard = new ArrayList<>();
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT stat.id, elo, wins, defeats, fk_user, username, token, coins, name, bio, image FROM stat " +
                            "LEFT JOIN \"user\" u on u.id = stat.fk_user " +
                            "ORDER BY elo DESC"
            );
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString(1) + "|" + rs.getInt(2) + "|" +
                        rs.getInt(3) + "|" + rs.getInt(4) + "|" +
                        rs.getString(5) + "|" + rs.getString(6) + "|" +
                        rs.getString(7) + "|" + rs.getInt(8) + "|" +
                        rs.getString(9) + "|" + rs.getString(10) + "|" +
                        rs.getString(11)
                );
                User user = new User(rs.getString(5), rs.getString(6),
                        rs.getString(7), rs.getInt(8), rs.getString(9),
                        rs.getString(10), rs.getString(11)
                );
                Stat stat = new Stat(rs.getString(1), rs.getInt(2), rs.getInt(3),
                        rs.getInt(4), rs.getString(5));

                scoreboard.add(new Scoreboard(user, stat));
            }

            if (scoreboard.isEmpty()) {
                return new Response(400, "{ \"message\": \"Scoreboard konnten nicht ausgelesen werden\" }");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"Scoreboard konnten nicht ausgelesen werden\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\": \"Scoreboard konnten erfolgreich ausgelesen werden\", " +
                "\"stats\": " + scoreboard +" }");
    }
}
