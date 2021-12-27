package com.company.Server.DatabaseAccess;

import com.company.Server.models.Card;
import com.company.Server.models.Response;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeckAccess extends DBAccess {
    public DeckAccess() throws SQLException {
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
}
