package com.company.Server.DatabaseAccess;

import com.company.Server.models.Response;
import com.company.Server.models.Stat;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class StatAccess extends DBAccess{
    public StatAccess() throws SQLException {
    }

    public void create(Stat stat) {
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
        }
    }

    public Response read(String token) {
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
        }
        return new Response(200, "{ \"message\": \"Stats konnten erfolgreich ausgelesen werden\", " +
                "\"stats\": " + stat +" }");
    }
}
