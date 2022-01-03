package com.company.Server.DatabaseAccess;

import com.company.Server.models.Card;
import com.company.Server.models.Package;
import com.company.Server.models.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardAccess extends DBAccess{

    public CardAccess() throws SQLException {
    }

    public Response createCards(String json, ObjectMapper objectMapper) throws SQLException, JsonProcessingException {
        //Delete all whitespace from JSON-String
        json = json.replaceAll("\\s*", "");
        System.out.println("JSON:\n" + json);
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(json);

        ArrayList<Card> cards = new ArrayList<>();

        /**
         * Packages werden auch erstellt, wenn Cards nicht erstellt werden können
         * Dadurch gibt es auch Packages, die keine Karten beinhalten.
         * TODO: Nur Package erstellen, wenn auch Cards erstellt werden.
         */
        Package pack = new Package(cards);
        try {
            //Package erstellen
            PreparedStatement createPackage = connection.prepareStatement(
                    "INSERT INTO mtcg.public.package (id) " +
                            "VALUES (?);"
            );
            createPackage.setString(1, pack.getId());
            createPackage.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        //Split JSON into several objects to create several cards
        while (matcher.find()) {
            Card card = objectMapper.readValue(matcher.group(0), Card.class);
            cards.add(card);

            try {
                //Create cards
                PreparedStatement create = connection.prepareStatement(
                        "INSERT INTO mtcg.public.card (id, name, damage, card_package_id_fk) " +
                                "VALUES (?,?,?,?);"
                );

                create.setString(1, card.getId());
                create.setString(2, card.getName());
                create.setDouble(3, card.getDamage());
                create.setString(4, pack.getId());

                create.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                deletePackage(pack);
                return new Response(400, "{ \"message\" : \"Cards konnten nicht erstellt werden\" }");
            }
        }
        pack.setCards(cards);
        System.out.println(pack.getCards());

        return new Response(200, "{ \"message\" : \"Cards erfolgreich erstellt\" }");
    }

    public Response readCards(String token) {
        ArrayList<Card> cards = new ArrayList<>();
        try {
            //Package erstellen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card INNER JOIN package p on p.id = card.card_package_id_fk INNER JOIN mtcg.public.user u on u.id = p.fk_user WHERE token = ?"
            );
            read.setString(1, token);
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                cards.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            System.out.println(cards);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Cards konnten nicht gelesen werden\" }");
        }


        return new Response(200, "{ \"message\" : \"Cards erfolgreich ausgelesen\", " +
                "\"cards\":" + cards +" }");
    }

    /**
     * TODO: Funktion nicht einheitlich gelöst, mal wird user.id verwendet, mal user.token
     * TODO: Funtkion einheiltich machen
     * TODO: Transaktion nur durchführen wenn package wirklich erhalten
     * TODO: UserAccess? oder doch alles in einer Funktion lösen?
     *
     * @param token
     * @return
     * @throws SQLException
     */
    public Response acquire(String token) throws SQLException {
        System.out.println("ACQUIREEEEEE token: " + token);
        //Kontrollieren ob genügend coins vorhanden sind
        int coins = new UserAccess().getCoins(token);
        System.out.println("Coins davor: " + coins);

        //Wenn genügend coins vorhanden sind Package kaufen
        if (coins>0) {
            PreparedStatement choosePackage = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.package WHERE fk_user IS NULL"
            );
            ResultSet rs = choosePackage.executeQuery();
            if (rs.next()) {
                String userId = new UserAccess().getId(token);
                System.out.println(userId);
                PreparedStatement acquirePackage = connection.prepareStatement(
                        "UPDATE mtcg.public.package SET fk_user = (SELECT id FROM mtcg.public.user WHERE token = ?) WHERE package.id = ?"
                );
                acquirePackage.setString(1, token);
                acquirePackage.setString(2, rs.getString(1));
                acquirePackage.executeUpdate();
            } else {
                return new Response(400, "{ \"message\" : \"No packages avaliable\" }");
            }


            //Checken, ob Transaktion erfolgreich war
            coins = new UserAccess().buyPackage(token);
            if (coins == -1) {
                System.out.println(coins);
                //Wenn nicht erfolgreich --> Fehlermeldung
                return new Response(400, "{ \"message\" : \"Package could not be acquired\" }");
            }
            System.out.println("Coins danach: " +coins);

            return new Response(200, "{ \"message\" : \"Package could be acquired\" }");
        }
        return new Response(400, "{ \"message\" : \"Package could not be acquired. To few coins.\" }");
    }

    public ArrayList<Card> get4Cards(String token) {
        ArrayList<Card> cards = new ArrayList<>();
        try {
            //Package erstellen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card " +
                            "INNER JOIN package p on p.id = card.card_package_id_fk " +
                            "INNER JOIN mtcg.public.user u on u.id = p.fk_user " +
                            "WHERE token = ? " +
                            "LIMIT 4;"
            );
            read.setString(1, token);
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                cards.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            System.out.println(cards);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return cards;
    }

    private void deletePackage(Package pack) {
        try {
            PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM mtcg.public.package WHERE id = ?"
            );
            delete.setString(1, pack.getId());
            delete.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully deleted package: " + pack.getId());
    }

    public boolean userHasCard(String userId, String cardId) {
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM card " +
                            "INNER JOIN package p on p.id = card.card_package_id_fk " +
                            "INNER JOIN \"user\" u on u.id = p.fk_user " +
                            "WHERE u.id = ? AND card.id = ?"
            );
            read.setString(1, userId);
            read.setString(2, cardId);
            ResultSet rs = read.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Card getCardByCardId(String cardId) {
        Card card = null;
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM card " +
                            "INNER JOIN package p on p.id = card.card_package_id_fk " +
                            "INNER JOIN \"user\" u on u.id = p.fk_user " +
                            "WHERE card.id = ?"
            );
            read.setString(1, cardId);
            ResultSet rs = read.executeQuery();

            if (rs.next()) {
                card = new Card(rs.getString(1), rs.getString(2), rs.getFloat(3));
                card.setPackageId(rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return card;
    }

    public Card getCardByTradeId(String tradeId) {
        Card card = null;
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM card " +
                            "INNER JOIN trade t on card.id = t.fk_card " +
                            "WHERE t.id = ?"
            );
            read.setString(1, tradeId);
            ResultSet rs = read.executeQuery();

            if (rs.next()) {
                card = new Card(rs.getString(1), rs.getString(2), rs.getFloat(3));
                card.setPackageId(rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return card;
    }

    public boolean deleteCard(Card card1) {
        try {
            PreparedStatement deleteCard = connection.prepareStatement(
                    "DELETE FROM card " +
                            "WHERE id = ?"
            );
            deleteCard.setString(1, card1.getId());
            deleteCard.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createCard(Card card, String packageId) {
        try {
            PreparedStatement createCard = connection.prepareStatement(
                    "INSERT INTO card (id, name, damage, card_package_id_fk) " +
                            "VALUES (?,?,?,?)"
            );
            createCard.setString(1, card.getId());
            createCard.setString(2, card.getName());
            createCard.setDouble(3, card.getDamage());
            createCard.setString(4, packageId);
            createCard.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean swapCards(Card card1, Card card2) {
        try {
            PreparedStatement swapCard1 = connection.prepareStatement(
                    "UPDATE card " +
                            "SET card_package_id_fk = ? " +
                            "WHERE id = ?"
            );
            swapCard1.setString(1, card2.getPackageId());
            swapCard1.setString(2, card1.getId());

            PreparedStatement swapCard2 = connection.prepareStatement(
                    "UPDATE card " +
                            "SET card_package_id_fk = ? " +
                            "WHERE id = ?"
            );
            swapCard1.setString(1, card1.getPackageId());
            swapCard1.setString(2, card2.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
