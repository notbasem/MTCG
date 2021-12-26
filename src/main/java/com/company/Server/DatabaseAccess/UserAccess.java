package com.company.Server.DatabaseAccess;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.company.Server.models.Response;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;

public class UserAccess extends DBAccess {
    public UserAccess() throws SQLException {
    }

    public Response createUser(User user) throws SQLException {
        PreparedStatement read = connection.prepareStatement(
                "SELECT username FROM mtcg.public.user WHERE username = ?"
        );
        read.setString(1, user.getUsername());
        ResultSet rs = read.executeQuery();
        //Wenn username nicht vorhanden ist, User erstellen
        if (!rs.next()) {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO mtcg.public.user (id, username, password, token, coins) " +
                            "VALUES (?,?,?,?,?);"
            );

            //Password hash erstellen mit: https://github.com/patrickfav/bcrypt
            user.setPassword(BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray()));

            create.setString(1, user.getId());
            create.setString(2, user.getUsername());
            create.setString(3, user.getPassword());
            create.setString(4, user.getToken());
            create.setInt(5, user.getCoins());
            create.executeUpdate();
            //sendResponse(exchange, 200, respText);

            System.out.println("Registrierung erfolgreich:");
            System.out.println("Username: " + user.getUsername() + " | Password: " + user.getPassword() + " | Token: " + user.getToken());
            return new Response(200, "{ \"message\": \"User erstellt\" }");
        } else {
            //sendResponse(exchange, 409, respText);
            System.out.println("Registrierung fehlgeschlagen.");
        }
        rs.close();
        read.close();

        return new Response(409, "{ \"message\": \"Username bereits vorhanden\" }");
    }

    public Response loginUser(User user) throws SQLException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        //Überprüfen, ob der username bereits vorhanden ist
        PreparedStatement read = connection.prepareStatement(
                "SELECT * FROM mtcg.public.user WHERE username = ?"
        );
        read.setString(1, user.getUsername());
        //Passwort hash des Users zum vergleichen holen
        ResultSet rs = read.executeQuery();
        String hash = null;
        while (rs.next()) {
            hash = rs.getString("password");
        }
        rs.close();
        read.close();

        //Passwörter miteinander vergleichen
        BCrypt.Result result = BCrypt.verifyer().verify(user.getPassword().toCharArray(), hash);

        if (result.verified) {
            String userJson = objectMapper.writeValueAsString(user);
            System.out.println("Login erfolgreich");

            return new Response(200,"{ \"message\": \"Login erfolgreich\", \"user\": " + userJson + " }" );
        } else {
            System.out.println("Login fehlgeschlagen");
            return new Response(401, "{ \"message\": \"Username und Passwort stimmen nicht überein\" }" );
        }
    }


}
