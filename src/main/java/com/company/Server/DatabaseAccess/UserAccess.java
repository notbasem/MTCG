package com.company.Server.DatabaseAccess;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.company.Server.models.Response;
import com.company.Server.models.Stat;
import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAccess extends DBAccess {
    public UserAccess() throws SQLException {
    }

    public Response createUser(User user) throws SQLException {
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT username FROM mtcg.public.user WHERE username = ?"
            );
            read.setString(1, user.getUsername());
            ResultSet rs = read.executeQuery();
            //Wenn username nicht vorhanden ist, User erstellen
            if (rs.next()) {
                return new Response(409, "{ \"message\": \"Username already in use\" }");
            }
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

            System.out.println("Registered successfully:");
            System.out.println("Username: " + user.getUsername() + " | Password: " + user.getPassword() + " | Token: " + user.getToken());
            return new Response(200, "{ \"message\": \"User registered successfully\" }");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\": \"User registered successfully\" }");
    }

    public Response loginUser(User user) throws SQLException {
        //Überprüfen, ob der username bereits vorhanden ist
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.user WHERE username = ?"
            );
            read.setString(1, user.getUsername());
            //Passwort hash des Users zum vergleichen holen
            ResultSet rs = read.executeQuery();
            String hash = null;
            if (rs.next()) {
                hash = rs.getString("password");
            }
            rs.close();
            read.close();

            if (hash == null) {
                return new Response().setLoginFailed();
            }

            //Passwörter miteinander vergleichen
            BCrypt.Result result = BCrypt.verifyer().verify(user.getPassword().toCharArray(), hash);
            if (result.verified) {
                System.out.println("Logged in successfully");
                return new Response(200,"{ \"message\": \"Logged in successfully\", \"user\": " + user + " }" );
            } else {
                return new Response().setLoginFailed();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response().setLoginFailed();
        } finally {
            connection.close();
        }
    }

    public Response read(String token) throws SQLException {
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
                return new Response(200, "{ \"message\": \"User read successfully\"," +
                        "\"user\":" + user + "}" );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"User could not be read\" }");
        } finally {
            connection.close();
        }
        return new Response(400, "{ \"message\": \"User could not be read\" }");
    }

    public Response update(String body, String token) throws SQLException {
        String userId = new UserAccess().getId(token);
        if (userId == null) {
            return new Response().setNotAuthorized();
        }
        Map<String, String> json = new TreeMap<>();
        Pattern p = Pattern.compile("\"(\\w+)\":\\s*\"(.*?)\"");
        Matcher m = p.matcher(body);

        while (m.find()) {
            System.out.println(m.group(0));
            json.put(m.group(1), m.group(2));
        }

        if (json.size() < 3) {
            return new Response(400, "{ \"message\": \"User could not be updated\" }");
        }

        try {
            PreparedStatement updateUser = connection.prepareStatement(
                    "UPDATE mtcg.public.user SET name=?, bio=?, image=? WHERE token = ?"
            );
            updateUser.setString(1, json.get("Name"));
            updateUser.setString(2, json.get("Bio"));
            updateUser.setString(3, json.get("Image"));
            updateUser.setString(4, token);
            updateUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\": \"User could not be updated\" }");
        } finally {
            connection.close();
        }
        return new Response(200,"{ \"message\": \"User updated successfully\" }" );
    }

    public int getCoins(String token) throws SQLException {
        try {
            PreparedStatement getCoins = connection.prepareStatement(
                    "SELECT coins FROM mtcg.public.user WHERE token = ?"
            );
            getCoins.setString(1, token);

            ResultSet rs = getCoins.executeQuery();
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

    public int buyPackage(String token) throws SQLException {
        if (getCoins(token) <= 0) {
            return -1;
        }
        try {
            PreparedStatement buyPackage = connection.prepareStatement(
                    "UPDATE mtcg.public.user SET coins = coins-5 WHERE token = ?"
            );
            buyPackage.setString(1, token);
            buyPackage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getCoins(token);
    }

    public String getId(String token) throws SQLException {
        try {
            PreparedStatement getId = connection.prepareStatement(
                    "SELECT id FROM mtcg.public.user WHERE token = ?"
            );
            getId.setString(1, token);

            ResultSet rs = getId.executeQuery();
            if (rs.next()) {
                 return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return null;
    }

    public User getUser(String id) throws SQLException {
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
        } finally {
            connection.close();
        }
        return user;
    }
}
