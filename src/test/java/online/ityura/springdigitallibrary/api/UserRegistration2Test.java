package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.restassured.RestAssured.given;


public class UserRegistration2Test extends BaseApiTest {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/spring_digital_bookstore";
    private static final String DB_USERNAME = "nobugs228";
    private static final String DB_PASSWORD = "nobugs228PASSWORD!#";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    @Test
    void userCanLoginWithValidData() {
//        Create user
        given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(List.of(new CustomLoggingFilter()))
                .body("""
                        {
                          "nickname": "TestUser1",
                          "email": "TestUser1@example.com",
                          "password": "TestUser1!"
                        }
                        """)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201);

//        Login as admin
        LoginResponse response = given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(List.of(new CustomLoggingFilter()))
                .body("""
                        {
                          "email": "admin@gmail.com",
                          "password": "admin"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().as(LoginResponse.class);

        String accessToken = response.getAccessToken();

//        Check if user exists thru admin request
        List<AdminUserResponse> users = given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken )
                .filters(List.of(new CustomLoggingFilter()))
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<AdminUserResponse>>() {});

        Boolean isUserExist = users.stream().anyMatch(user -> user.getNickname().equals("TestUser1")
        && user.getEmail().equals("TestUser1@example.com"));
        softly.assertThat(isUserExist).isTrue();

//        Check if user exists in database directly using JDBC
        try (Connection connection = getConnection()) {
            String sql = "SELECT id, nickname, email, password_hash, role, created_at FROM users WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "TestUser1@example.com");
                try (ResultSet resultSet = statement.executeQuery()) {
                    softly.assertThat(resultSet.next()).isTrue();
                    softly.assertThat(resultSet.getLong("id")).isInstanceOf(Long.class);
                    softly.assertThat(resultSet.getString("nickname")).isEqualTo("TestUser1");
                    softly.assertThat(resultSet.getString("email")).isEqualTo("TestUser1@example.com");
                    softly.assertThat(resultSet.getString("password_hash")).isNotNull();
                    softly.assertThat(resultSet.getString("role")).isEqualTo("USER");
                    softly.assertThat(resultSet.getTimestamp("created_at")).isNotNull();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user in database", e);
        }
    }


}
