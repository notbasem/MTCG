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
                create.setFloat(3, card.getDamage());
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

    public Response readCards() {
        ArrayList<Card> cards = new ArrayList<>();
        try {
            //Package erstellen
            PreparedStatement read = connection.prepareStatement(
                    "SELECT * FROM mtcg.public.card "
            );
            ResultSet rs = read.executeQuery();

            while (rs.next()) {
                cards.add(new Card(rs.getString(1), rs.getString(2), rs.getFloat(3)));
            }
            System.out.println(cards);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(400, "{ \"message\" : \"Cards konnten nicht gelesen werden\" }");
        }

        return new Response(200, "{ \"message\" : \"Cards erfolgreich erstellt\", " +
                "\"cards\":" + cards +" }");
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
}
