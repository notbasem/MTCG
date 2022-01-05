package com.company.Server.DatabaseAccess;

import com.company.Server.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

public class BattleAccess extends DBAccess {
    public BattleAccess() throws SQLException {
    }

    public Battle createOrJoin(String token) throws SQLException {
        Battle battle = null;
        try {
            PreparedStatement createOrJoin = connection.prepareStatement(
                    "SELECT * FROM battle WHERE fk_player2 IS NULL LIMIT 1"
            );
            ResultSet rs = createOrJoin.executeQuery();

            if (rs.next()) {
                battle = getBattle(rs.getString(1));
            } else {
                battle = createBattle();
            }

            battle = addPlayer(battle, token);
            if (battle == null) {
                System.out.println("Bereits ein offenes Battle vorhanden");
                return null;
            }

            System.out.println(battle);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    private Battle createBattle () throws SQLException {
        Battle battle = null;
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO battle (id, fk_player1, fk_player2, fk_winner) VALUES (?,?,?,?)"
            );
            String id = UUID.randomUUID().toString();
            create.setString(1, id);
            create.setString(2, null);
            create.setString(3, null);
            create.setString(4, null);
            create.execute();

            battle = new Battle(id, null, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    private Battle addPlayer (Battle battle, String token) throws SQLException {
        try {
            String userId = new UserAccess().getId(token);
            PreparedStatement create;
            if (battle.getPlayer1() == null) {
                System.out.println("PLAYER1");
                create = connection.prepareStatement(
                        "UPDATE battle SET fk_player1 = ? WHERE id = ?"
                );
                battle.setPlayer1(userId);
            } else if (battle.getPlayer2() == null && !battle.getPlayer1().equals(userId)){
                System.out.println("PLAYER2");
                create = connection.prepareStatement(
                        "UPDATE battle SET fk_player2 = ? WHERE id = ?"
                );
                battle.setPlayer2(userId);
            } else {
                System.out.println("Bereits ein offenes Battle vorhanden");
                return null;
            }
            create.setString(1, userId);
            create.setString(2, battle.getId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return battle;
    }

    private Battle getBattle (String battleId) throws SQLException {
        System.out.println(battleId);
        Battle battle = null;
        try {
            PreparedStatement create = connection.prepareStatement(
                    "SELECT * FROM battle WHERE id = ?"
            );
            create.setString(1, battleId);
            ResultSet rs = create.executeQuery();

            if (rs.next()) {
                battle = new Battle(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return battle;
    }

    public Response battle(Battle battle) throws SQLException {
        if (battle == null) {
            return new Response(400, "{ \"message\" : \"Bereits ein offenes Battle vorhanden\"}");
        } else if (battle.getPlayer1() == null || battle.getPlayer2() == null) {
            return new Response(200, "{ \"message\" : \"Warten auf Spieler...\"}");
        }
        User player1 = new UserAccess().getUser(battle.getPlayer1());
        User player2 = new UserAccess().getUser(battle.getPlayer2());
        System.out.println("PLAYER 1: " + player1);
        System.out.println("PLAYER 2: " + player2);

        Deck deck1 = new DeckAccess().getDeck(battle.getPlayer1());
        Deck deck2 = new DeckAccess().getDeck(battle.getPlayer2());

        //Max 100 Runden möglich
        for (int i = 0; i < 100; i++) {
            System.out.println("Round " + (i+1) + ":");
            System.out.println("Size Deck1:" + deck1.getCards().size());
            System.out.println("Size Deck2:" + deck2.getCards().size());
            if (deck1.getCards().size() == 0) {
                battle.setWinner(player2.getId());
                System.out.println("Player2 hat gewonnen");
                setWinner(battle);
                updateStats(battle);
                return new Response(200, "{ \"message\" : \"Battle abgeschlossen\"," +
                        "\"Winner:\" " + player1 +
                        "}");
            } else if (deck2.getCards().size() == 0) {
                battle.setWinner(player1.getId());
                System.out.println("Player1 hat gewonnen");
                setWinner(battle);
                updateStats(battle);
                return new Response(200, "{ \"message\" : \"Battle abgeschlossen\"," +
                        "\"Winner:\" " + player2 +
                        "}");
            }

            int random1 = new Random().nextInt(deck1.getCards().size());
            Card card1 = deck1.getCards().get(random1);
            int random2 = new Random().nextInt(deck2.getCards().size());
            Card card2 = deck2.getCards().get(random2);
            Card winner=null;
            System.out.println(card1);
            System.out.println(card2);

            if (cardWins(card1, card2) || calcDamage(card1, card2) > calcDamage(card2, card1)) {
                winner = card1;
                deck2.getCards().remove(card2);
                deck1.getCards().add(card2);
                System.out.println("Winner: " + card1);
            } else if (cardWins(card2, card1) || calcDamage(card2, card1) > calcDamage(card1, card2)) {
                winner = card2;
                deck1.getCards().remove(card1);
                deck2.getCards().add(card1);
                System.out.println("Winner: " + card2);
            }

            if (winner != null) {
                System.out.println("Winner: " + winner.getName() + "(" + winner.getDamage() + ")");
            }

            newRound(new Round(card1, card2, winner, battle));
        }

        //Gewinner eintragen und stats anpassen
        if (deck1.getCards().size() > deck2.getCards().size()) {
            battle.setWinner(player1.getId());
            System.out.println("Player1 hat gewonnen");
            setWinner(battle);
            updateStats(battle);
            return new Response(200, "{ \"message\" : \"Battle abgeschlossen\"," +
                    "\"Winner:\" " + player1 +
                    "}");
        } else if (deck2.getCards().size() > deck1.getCards().size()) {
            battle.setWinner(player2.getId());
            System.out.println("Player2 hat gewonnen");
            setWinner(battle);
            updateStats(battle);
            return new Response(200, "{ \"message\" : \"Battle abgeschlossen\"," +
                    "\"Winner:\" " + player2 +
                    "}");
        }

        //Wenn der Spieler noch kein offenes Spiel hat, dann tritt er einem Spiel bei
        return new Response(200, "{ \"message\" : \"Spiel ist unentschieden ausgegangen\"}");
    }

    private boolean cardWins(Card card1, Card card2) {
        //Monster-Only battles
        if (card1.getType().equals("Monster") && card2.getType().equals("Monster")) {
            System.out.println("MONSTERFIGHT");
            //Dragon > Goblin
            if (card1.getName().contains("Dragon") && card2.getName().contains("Goblin")) {
                System.out.println("DRINNE AMK");
                return true;
            }
            //Wizard > Ork
            if (card1.getName().contains("Wizard") && card2.getName().contains("Ork")) {
                System.out.println("DRINNE AMK");
                return true;
            }

            //FireElves > Dragon
            if (card1.getName().contains("FireElves") && card2.getName().contains("Dragon")) {
                System.out.println("DRINNE AMK");
                return true;
            }
        }

        //Monster-Spell battles
        if (card1.getType().equals("Monster") && card2.getType().equals("Spell")) {
            System.out.println("MONSTER-SPELL-FIGHT");
            //Kraken > all Spell
            if (card1.getName().contains("Kraken")) {
                System.out.println("DRINNE AMK");
                return true;
            }
        }

        //Spell-Monster battles
        if (card1.getType().equals("Spell") && card2.getType().equals("Monster")) {
            System.out.println("SPELL-MONSTER-FIGHT");
            //WaterSpell > Knight
            if (card1.getName().contains("Water") && card2.getName().equals("Knight")) {
                System.out.println("DRINNE AMK");
                System.out.println("WATERSPELLKNIGHT");
                return true;
            }
        }
        return false;
    }

    private double calcDamage(Card card1, Card card2) {
        if (card1.getName().contains("Spell")) {
            //Effektive Spells
            if (card1.getName().contains("Water") && card2.getName().contains("Fire") ||
                    card1.getName().contains("Fire") && card2.getName().contains("Normal") ||
                    card1.getName().contains("Normal") && card2.getName().contains("Water")
            ) {
                System.out.println("DamageNotNormal: " + card1.getDamage()*2);
                return card1.getDamage()*2;
            }

            //Nicht Effektive Spells
            if (card1.getName().contains("Fire") && card2.getName().contains("Water") ||
                    card1.getName().contains("Normal") && card2.getName().contains("Fire") ||
                    card1.getName().contains("Water") && card2.getName().contains("Normal")
            ) {
                System.out.println("DamageNotNormal: " + card1.getDamage()/2);
                return card1.getDamage()/2;
            }
        }
        //Beide gleicher Element-Type (kein Effekt)
        return card1.getDamage();
    }

    private void newRound(Round round) throws SQLException {
        try {
            PreparedStatement create = connection.prepareStatement(
                    "INSERT INTO round (id, fk_card1, fk_card2, fk_winner_card, fk_battle) VALUES (?,?,?,?,?)"
            );
            create.setString(1, round.getId());
            create.setString(2, round.getCard1().getId());
            create.setString(3, round.getCard2().getId());
            if (round.getWinner() != null) {
                create.setString(4, round.getWinner().getId());
            } else {
                create.setString(4, null);
            }
            create.setString(5, round.getBattle().getId());
            create.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setWinner(Battle battle) throws SQLException {
        try {
            PreparedStatement setWinner = connection.prepareStatement(
                    "UPDATE battle SET fk_winner = ? WHERE id = ?"
            );
            setWinner.setString(1, battle.getWinner());
            setWinner.setString(2, battle.getId());
            setWinner.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats(Battle battle) throws SQLException {
        try {
            PreparedStatement updateWinner = connection.prepareStatement(
                    "UPDATE stat SET elo = elo + 3 WHERE fk_user = ?"
            );
            updateWinner.setString(1, battle.getWinner());
            updateWinner.executeUpdate();
            String loserId;
            if (battle.getPlayer1().equals(battle.getWinner())){
                loserId = battle.getPlayer2();
            } else {
                loserId = battle.getPlayer1();
            }

            PreparedStatement updateLoser = connection.prepareStatement(
                    "UPDATE stat SET elo = elo -5 WHERE fk_user = ?"
            );
            updateLoser.setString(1, loserId);
            updateLoser.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
