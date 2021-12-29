package com.company.Server.DatabaseAccess;

import com.company.Server.models.Stat;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class StatAccess extends DBAccess{
    public StatAccess() throws SQLException {
    }

    public void create(Stat stat) {
        System.out.println(stat.getUser().getId());
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO mtcg.public.stat (id, fk_user) VALUES (?,?)"
            );
            create.setString(1, stat.getId());
            create.setString(2, stat.getUser().getId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
