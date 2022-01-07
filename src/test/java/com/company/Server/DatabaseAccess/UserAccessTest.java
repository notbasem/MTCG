package com.company.Server.DatabaseAccess;

import com.company.Server.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserAccessTest {
    User user1 = new User("testuser", "testuser");
    User user2 = new User("falseuser", "falseuser");

    @BeforeEach
    void setup() {
        user1.setId("9788b3de-e2b7-48fc-a573-592318d11881");
    }

    @Test
    @Order(1)
    void testCreateUserTrue() throws SQLException {
        assertEquals(200, new UserAccess().createUser(user1).getStatus());
    }

    @Test
    @Order(2)
    void testCreateUserFalse() throws SQLException {
        assertEquals(409, new UserAccess().createUser(user1).getStatus());
    }

    @Test
    @Order(3)
    void loginUserTrue() throws SQLException, JsonProcessingException {
        assertEquals(200, new UserAccess().loginUser(user1).getStatus());
    }

    @Test
    @Order(4)
    void loginUserFalse() throws SQLException, JsonProcessingException {
        assertEquals(401, new UserAccess().loginUser(user2).getStatus());
    }

    @Test
    @Order(5)
    void testReadTrue() throws SQLException {
        assertEquals(200, new UserAccess().read(user1.getToken()).getStatus());
    }

    @Test
    @Order(6)
    void testReadFalse() throws SQLException {
        assertEquals(400, new UserAccess().read(user2.getToken()).getStatus());
    }

    @Test
    @Order(7)
    void testUpdateTrue() throws SQLException {
        String body = "{\n" +
                "  \"Name\": \"TestUser\",\n" +
                "  \"Bio\": \"me swaggin...\",\n" +
                "  \"Image\": \":-|\"\n" +
                "}";
        assertEquals(200, new UserAccess().update(body, user1.getToken()).getStatus());
    }

    @Test
    @Order(8)
    void testUpdateFalse() throws SQLException {
        String body = "{\n" +
                "  \"Name\": \"false\",\n" +
                "  \"Bio\": \"false...\",\n" +
                "  \"Image\": \":'-(\"\n" +
                "}";
        assertEquals(400, new UserAccess().update(body, user2.getToken()).getStatus());
    }

    @Test
    @Order(9)
    void testUpdateFalseJSON() throws SQLException {
        String body = "{\n" +
                "  \"Name\": \"false\",\n" +
                "  \"Bio\": \"false...\",\n" +
                "}";
        assertEquals(400, new UserAccess().update(body, user1.getToken()).getStatus());
    }


    @Test
    @Order(10)
    void testGetCoinsTrue() throws SQLException {
        assertEquals(20, new UserAccess().getCoins(user1.getToken()));
    }

    @Test
    @Order(11)
    void testGetCoinsFalse() throws SQLException {
        assertEquals(-1, new UserAccess().getCoins(user2.getToken()));
    }


    @Test
    @Order(12)
    void testBuyPackageTrue() throws SQLException {
        assertEquals(15, new UserAccess().buyPackage(user1.getToken()));
    }

    @Test
    @Order(13)
    void testBuyPackageFalse() throws SQLException {
        assertEquals(-1, new UserAccess().buyPackage(user2.getToken()));
    }

}