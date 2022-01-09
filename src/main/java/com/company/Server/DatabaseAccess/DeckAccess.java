package com.company.Server.DatabaseAccess;

import com.company.Server.models.Card;
import com.company.Server.models.Deck;
import com.company.Server.models.Response;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public Response read(String token) throws SQLException {
        ArrayList<Card> deck = new ArrayList<>();
        System.out.println("Is Deck Set: " + isDeckSet(token));
        if (!isDeckSet(token)) { //Wenn das Deck noch nicht gesetzt wurde
            try {
                deck = new CardAccess().get4Cards(token);
                //Wenn der User keine Karten hat Fehlermeldung zurückgeben
                if (deck.isEmpty()) {
                    return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
                }
                PreparedStatement read = connection.prepareStatement(
                        "UPDATE deck SET fk_card1 = ?, fk_card2 = ?, fk_card3 = ?, fk_card4 = ? " +
                                "WHERE fk_user = (SELECT id FROM mtcg.public.user u WHERE token = ?)"
                );
                read.setString(1, deck.get(0).getId());
                read.setString(2, deck.get(1).getId());
                read.setString(3, deck.get(2).getId());
                read.setString(4, deck.get(3).getId());
                read.setString(5, token);
                read.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
            }
        } else { //Wenn das Deck bereits gesetzt wurde
            try {
                //Alle Karten auslesen, die dem User mit dem passenden Token gehören
                PreparedStatement read2 = connection.prepareStatement(
                        "SELECT * FROM mtcg.public.card " +
                                "INNER JOIN deck d on card.id = d.fk_card1 " +
                                "OR card.id = d.fk_card2 " +
                                "OR card.id = d.fk_card3 " +
                                "OR card.id = d.fk_card4 " +
                                "INNER JOIN mtcg.public.user u on u.id = d.fk_user " +
                                "WHERE u.token = ?"
                );
                read2.setString(1, token);
                ResultSet rs = read2.executeQuery();

                while (rs.next()) {
                    System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
                    deck.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
            }
        }
        connection.close();
        return new Response(200, "{ \"message\" : \"Deck read successfully\", " +
                "\"deck\":" + deck +" }");
    }

    public Response configure(String body, String token) throws SQLException {
        String userId = new UserAccess().getId(token);
        //Mit Regex alles außer ID's und Beistriche entfernen
        String[] cards = body.replaceAll("[^a-zA-Z0-9-,]", "").split(",");
        if (cards.length < 4) {
            return new Response(400, "{ \"message\" : \"Deck could not be configured\" }");
        }
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
            return new Response(400, "{ \"message\" : \"Deck could not be configured\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\" : \"Deck configured successfully\"}");
    }

    private boolean isDeckSet(String token) {
        try {
            PreparedStatement readDeck = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.deck INNER JOIN mtcg.public.user u on deck.fk_user = u.id " +
                            "WHERE u.token = ? AND (fk_card1 IS NOT NULL " +
                            "OR fk_card2 IS NOT NULL " +
                            "OR fk_card3 IS NOT NULL " +
                            "OR fk_card4 IS NOT NULL)"
            );
            readDeck.setString(1, token);
            ResultSet rs = readDeck.executeQuery();

            if (!rs.next()) {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Response readPlain(String token) throws SQLException {
        ArrayList<Card> deck = new ArrayList<>();
        System.out.println("Is Deck Set: " + isDeckSet(token));
        if (!isDeckSet(token)) { //Wenn das Deck noch nicht gesetzt wurde
            try {
                deck = new CardAccess().get4Cards(token);
                //Wenn der User keine Karten hat Fehlermeldung zurückgeben
                if (deck.isEmpty()) {
                    return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
                }
                PreparedStatement read = connection.prepareStatement(
                        "UPDATE deck SET fk_card1 = ?, fk_card2 = ?, fk_card3 = ?, fk_card4 = ? " +
                                "WHERE fk_user = (SELECT id FROM mtcg.public.user u WHERE token = ?)"
                );
                read.setString(1, deck.get(0).getId());
                read.setString(2, deck.get(1).getId());
                read.setString(3, deck.get(2).getId());
                read.setString(4, deck.get(3).getId());
                read.setString(5, token);
                read.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
            } finally {
                connection.close();
            }
        } else { //Wenn das Deck bereits gesetzt wurde
            try {
                //Alle Karten auslesen, die dem User mit dem passenden Token gehören
                PreparedStatement read2 = connection.prepareStatement(
                        "SELECT * FROM mtcg.public.card " +
                                "INNER JOIN deck d on card.id = d.fk_card1 " +
                                "OR card.id = d.fk_card2 " +
                                "OR card.id = d.fk_card3 " +
                                "OR card.id = d.fk_card4 " +
                                "INNER JOIN mtcg.public.user u on u.id = d.fk_user " +
                                "WHERE u.token = ?"
                );
                read2.setString(1, token);
                ResultSet rs = read2.executeQuery();

                while (rs.next()) {
                    System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
                    deck.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new Response(400, "{ \"message\" : \"Deck could not be read\" }");
            }  finally {
                connection.close();
            }
        }
        return new Response(200, "{ \"deck\":" + deck +" }");
    }

    public Deck getDeck(String userId) throws SQLException {
        Deck deck = null;
        List<Card> cards = new ArrayList<>();
        try {
            PreparedStatement getDeck = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.deck " +
                            "WHERE fk_user = ?"
            );
            getDeck.setString(1, userId);
            ResultSet rsDeck = getDeck.executeQuery();

            if (rsDeck.next()) {
                deck = new Deck(rsDeck.getString(1));
            }

            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card " +
                            "INNER JOIN deck d on card.id = d.fk_card1 " +
                            "OR card.id = d.fk_card2 " +
                            "OR card.id = d.fk_card3 " +
                            "OR card.id = d.fk_card4 " +
                            "INNER JOIN mtcg.public.user u on u.id = d.fk_user " +
                            "WHERE u.id = ?"
            );
            read.setString(1, userId);
            ResultSet rs = read.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
                cards.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            deck.setCards(cards);
        } catch (SQLException e) {
            e.printStackTrace();
        }  finally {
            connection.close();
        }
        return deck;
    }

}
