package com.company.Server.DatabaseAccess;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.company.Server.models.Deck;
import com.company.Server.models.Response;
import com.company.Server.models.Stat;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.Tree;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            //Deck für User erstellen
            new DeckAccess().create(user);

            //Stat für User erstellen
            new StatAccess().create(new Stat(user.getId()));

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

    public Response read(String token) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        String userJson = "";
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.user WHERE token = ?"
            );
            getUser.setString(1, token);
            ResultSet rs = getUser.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getString(1), rs.getString(2),
                        rs.getString(4), rs.getInt(5), rs.getString(6),
                        rs.getString(7), rs.getString(8)
                );
                userJson = objectMapper.writeValueAsString(user);
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"User konnte nicht ausgelesen werden\" }");
        }
        return new Response(200, "{ \"message\": \"User konnte erfolgreich ausgelesen werden\"," +
                "\"user\":" + userJson + "}" );
    }

    public Response update(String body, String token) {
        Map<String, String> json = new TreeMap<>();
        Pattern p = Pattern.compile("\"(\\w+)\":\\s*\"(.*?)\"");
        Matcher m = p.matcher(body);

        while (m.find()) {
            System.out.println(m.group(0));
            json.put(m.group(1), m.group(2));
        }

        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "UPDATE mtcg.public.user SET name=?, bio=?, image=? WHERE token = ?"
            );
            getUser.setString(1, json.get("Name"));
            getUser.setString(2, json.get("Bio"));
            getUser.setString(3, json.get("Image"));
            getUser.setString(4, token);
            getUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"User konnte nicht geupdated werden\" }");
        }
        return new Response(200,"{ \"message\": \"User erfolgreich geupdated\" }" );
    }

    public int getCoins(String token) {
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT coins FROM mtcg.public.user WHERE token = ?"
            );
            getUser.setString(1, token);

            ResultSet rs = getUser.executeQuery();
            int coins = -1;
            if (rs.next()) {
                coins = rs.getInt(1);
            }
            return coins;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int buyPackage(String token) {
        if (getCoins(token) <= 0) {
            return -1;
        }
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "UPDATE mtcg.public.user SET coins = coins-5 WHERE token = ?"
            );
            getUser.setString(1, token);
            getUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getCoins(token);
    }

    public String getId(String token) {
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT id FROM mtcg.public.user WHERE token = ?"
            );
            getUser.setString(1, token);

            ResultSet rs = getUser.executeQuery();
            if (rs.next()) {
                 return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(String id) {
        User user = null;
        try {
            PreparedStatement getUser = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.user WHERE id = ?"
            );
            getUser.setString(1, id);
            ResultSet rs = getUser.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1)+"|"+ rs.getString(2)+"|"+
                        rs.getString(4)+"|"+ rs.getInt(5)+"|"+ rs.getString(6)+"|"+
                        rs.getString(7)+"|"+ rs.getString(8));
                user = new User(rs.getString(1), rs.getString(2),
                        rs.getString(4), rs.getInt(5), rs.getString(6),
                        rs.getString(7), rs.getString(8)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}
