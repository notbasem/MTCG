package com.company.Server.DatabaseAccess;

import com.company.Server.models.Card;
import com.company.Server.models.Response;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class DeckAccess extends DBAccess {
    public DeckAccess() throws SQLException {
    }

    public void create(User user) {
        System.out.println(user.getId());
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO mtcg.public.deck (id, fk_user) VALUES (?,?)"
            );
            create.setString(1, UUID.randomUUID().toString());
            create.setString(2, user.getId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Response read(String token) {
        ArrayList<Card> deck = new ArrayList<>();
        try {
            //Package erstellen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card INNER JOIN package p on p.id = card.card_package_id_fk INNER JOIN mtcg.public.user u on u.id = p.fk_user WHERE token = ?"
            );
            read.setString(1, token);
            ResultSet rs = read.executeQuery();

            while (rs.next() && deck.size()<4) {
                deck.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            System.out.println(deck);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Deck konnte nicht ausgelesen werden\" }");
        }
        return new Response(200, "{ \"message\" : \"Deck erfolgreich ausgelesen\", " +
                "\"deck\":" + deck +" }");
    }

    public Response configure(String body, String token) throws SQLException {
        String userId = new UserAccess().getId(token);
        //Mit Regex alles außer ID's und Beistriche entfernen
        String[] cards = body.replaceAll("[^a-zA-Z0-9-,]", "").split(",");
        try {
            PreparedStatement update = connection.prepareStatement(
                    "UPDATE deck SET fk_card1 = ?, fk_card2 = ?, fk_card3 = ?, fk_card4 = ?" +
                            "WHERE fk_user = ?"
            );
            update.setString(1, cards[0]);
            update.setString(2, cards[1]);
            update.setString(3, cards[2]);
            update.setString(4, cards[3]);
            update.setString(5, userId);

            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Deck konnte nicht überschrieben werden\" }");
        }
        return new Response(200, "{ \"message\" : \"Deck erfolgreich überschrieben\"}");
    }
}
