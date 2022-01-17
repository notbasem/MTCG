package com.company.Server.DatabaseAccess;

import com.company.Server.models.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CardAccessTest {
    User user1 = new User("9788b3de-e2b7-48fc-a573-592318d11881", "testuser", "testuser-mtcgToken", 20,  "false", "false...", "");
    User user2 = new User("falseuser", "falseuser");

    @Test
    @Order(1)
    void testAcquireTrue() throws SQLException { //Last package left
        assertEquals(200, new CardAccess().acquire(user1.getToken()).getStatus());
    }

    @Test
    @Order(2)
    void testAcquireFalse() throws SQLException { //No Packages left
        assertEquals(400, new CardAccess().acquire(user1.getToken()).getStatus());
    }

    @Test
    @Order(3)
    void testGet4CardsTrue() throws SQLException {
        assertEquals(4, new CardAccess().get4Cards(user1.getToken()).size());
    }

    @Test
    @Order(4)
    void testGet4CardsFalse() throws SQLException {
        assertEquals(0, new CardAccess().get4Cards(user2.getToken()).size());
    }

    @Test
    @Order(5)
    void testUserHasCardTrue() throws SQLException {
        assertTrue(new CardAccess().userHasCard(user1.getId(), "166c1fd5-4dcb-41a8-91cb-f45dcd57cef3"));
    }

    @Test
    @Order(6)
    void testUserHasCardFalseCardId() throws SQLException {
        assertFalse(new CardAccess().userHasCard(user1.getId(), "84d276ee-21ec-4171-a509-c1b88162831c"));
    }

    @Test
    @Order(7)
    void testUserHasCardFalseUserId() throws SQLException {
        assertFalse(new CardAccess().userHasCard(user2.getId(), "166c1fd5-4dcb-41a8-91cb-f45dcd57cef3"));
    }





}