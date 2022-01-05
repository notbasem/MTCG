package com.company.Server.DatabaseAccess;

import com.company.Server.models.Card;
import com.company.Server.models.Response;
import com.company.Server.models.Trade;
import com.company.Server.models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TradeAccess extends DBAccess{

    public TradeAccess() throws SQLException {
    }

    public Response read(String token) throws SQLException {
        List<Trade> trades = new ArrayList<>();
        try {
            String userId = new UserAccess().getId(token);
            if (userId == null) {
                return new Response(401, "{ \"message\": \"Not Authorized\" }");
            }
            //TODO: Nicht eigene Trades anzeigen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM trade"
            );
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                trades.add(new Trade(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getFloat(4))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Trades could not be read\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\" : \"Trades could be read successfully\"," +
                "\"Trades\": " + trades + "}");
    }

    public Response create(Trade trade, String token) throws SQLException {
        List<Trade> trades = new ArrayList<>();
        try {
            String userId = new UserAccess().getId(token);
            if (userId == null) {
                return new Response(401, "{ \"message\": \"Not Authorized\" }");
            }

            if (!new CardAccess().userHasCard(userId, trade.getCardToTrade())) {
                return new Response(400, "{ \"message\": \"User besitzt die Karte nicht\" }");
            }

            trade.setUserId(userId);

            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO trade (id, fk_card, type, \"minimumDamage\", fk_user) " +
                            "VALUES (?,?,?,?, ?);"
            );
            create.setString(1, trade.getId());
            create.setString(2, trade.getCardToTrade());
            create.setString(3, trade.getType());
            create.setFloat(4, trade.getMinimumDamage());
            create.setString(5, trade.getUserId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Trade could not be created\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\" : \"Trade could be created successfully\" }");
    }

    public Response delete(String tradeId, String token) throws SQLException {
        try {
            String userId = new UserAccess().getId(token);
            if (userId == null) {
                return new Response(401, "{ \"message\": \"Not Authorized\" }");
            }

            if (!userId.equals(getUserId(tradeId))) {
                return new Response(400, "{ \"message\": \"Trade could not be found\" }");
            }

            PreparedStatement create = connection.prepareStatement(
                    "DELETE FROM trade WHERE id = ?"
            );
            create.setString(1, tradeId);
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Trade could not be deleted\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\" : \"Trade successfully deleted\" }");
    }

    public Response trade(String tradeId, String cardId, String token) throws SQLException {
        try {
            //Checken ob User authorized ist
            //User1 ist der, der die Anfrage schickt
            String user1Id = new UserAccess().getId(token);
            if (user1Id == null) {
                return new Response(401, "{ \"message\": \"Not Authorized\" }");
            }

            //Checken ob User die Karte besitzt, die er traden will
            if (!(new CardAccess().userHasCard(user1Id, cardId))) {
                return new Response(400, "{ \"message\": \"User besitzt die Karte nicht\" }");
            }

            //Checken ob Trade existiert
            //User2 ist der, der den Trade erstellt hat
            String user2Id = getUserId(tradeId);
            if (user2Id == null) {
                return new Response(401, "{ \"message\": \"Trade does not exist\" }");
            }

            if (user1Id.equals(user2Id)) {
                return new Response(400, "{ \"message\" : \"Can't trade with yourself\" }");
            }

            Card card1 = new CardAccess().getCardByCardId(cardId);
            Card card2 = new CardAccess().getCardByTradeId(tradeId);
            String package1 = card1.getPackageId();
            String package2 = card2.getPackageId();

            System.out.println("DELETE1: " + new CardAccess().deleteCard(card1));
            System.out.println("DELETE2: " + new CardAccess().deleteCard(card2));

            System.out.println("CREATE1: " + new CardAccess().createCard(card1, package2));
            System.out.println("CREATE1: " + new CardAccess().createCard(card2, package1));
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Trade could not be fulfilled\" }");
        } finally {
            connection.close();
        }
        return new Response(200, "{ \"message\" : \"Trade fulfilled successfully\" }");
    }

    private String getUserId(String tradeId) throws SQLException {
        try {
            PreparedStatement read = connection.prepareStatement(
                    "SELECT fk_user FROM trade WHERE id = ?"
            );
            read.setString(1, tradeId);
            ResultSet rs = read.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
