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
         * Dadurch gibt es auch Packages, die keine Karten beinhalten. => werden bei Fehler
         * wieder gelöscht am Ende
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
                return new Response(400, "{ \"message\" : \"Cards could not be created\" }");
            }
        }
        pack.setCards(cards);
        System.out.println(pack.getCards());
        connection.close();
        return new Response(200, "{ \"message\" : \"Cards created successfully\" }");
    }

    public Response readCards(String token) throws SQLException {
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
            return new Response(400, "{ \"message\" : \"Cards could not be read\" }");
        } finally {
            connection.close();
        }


        return new Response(200, "{ \"message\" : \"Cards read successfully\", " +
                "\"cards\":" + cards +" }");
    }

    /**
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
        if (coins>=5) {
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
                connection.close();
                return new Response(400, "{ \"message\" : \"No packages avaliable\" }");
            }


            //Checken, ob Transaktion erfolgreich war
            coins = new UserAccess().buyPackage(token);
            if (coins == -1) {
                System.out.println(coins);
                //Wenn nicht erfolgreich --> Fehlermeldung
                connection.close();
                return new Response(400, "{ \"message\" : \"Package could not be acquired\" }");
            }
            System.out.println("Coins danach: " +coins);
            connection.close();
            return new Response(200, "{ \"message\" : \"Package acquired successfully\" }");
        }
        connection.close();
        return new Response(400, "{ \"message\" : \"Package could not be acquired. To few coins.\" }");
    }

    public ArrayList<Card> get4Cards(String token) throws SQLException {
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
        } finally {
            connection.close();
        }
        return cards;
    }

    private void deletePackage(Package pack) throws SQLException {
        try {
            PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM mtcg.public.package WHERE id = ?"
            );
            delete.setString(1, pack.getId());
            delete.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
        System.out.println("Successfully deleted package: " + pack.getId());
    }

    public boolean userHasCard(String userId, String cardId) throws SQLException {
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
        } finally {
            connection.close();
        }
        return false;
    }

    public Card getCardByCardId(String cardId) throws SQLException {
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
        } finally {
            connection.close();
        }
        return card;
    }

    public Card getCardByTradeId(String tradeId) throws SQLException {
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
        } finally {
            connection.close();
        }
        return card;
    }

    public boolean deleteCard(Card card1) throws SQLException {
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
        } finally {
            connection.close();
        }
        return true;
    }

    public boolean createCard(Card card, String packageId) throws SQLException {
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
        } finally {
            connection.close();
        }
        return true;
    }
}
