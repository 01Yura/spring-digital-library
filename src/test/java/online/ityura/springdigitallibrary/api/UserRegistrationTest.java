package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.Role;
import online.ityura.springdigitallibrary.testinfra.comparators.UniversalComparator;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDtoGeneratorWithFaker;
import online.ityura.springdigitallibrary.testinfra.generators.RandomModelGenerator;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static io.restassured.RestAssured.given;


public class UserRegistrationTest extends BaseApiTest {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/spring_digital_bookstore";
    private static final String DB_USERNAME = "nobugs228";
    private static final String DB_PASSWORD = "nobugs228PASSWORD!#";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    @Test
    void userCanLoginWithValidData() {
//        Arrangement
        RegisterRequest registerRequest = RandomDtoGeneratorWithFaker.generateRandomDtoObject(RegisterRequest.class);

//        Create user
        RegisterResponse registerResponse = given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new CustomLoggingFilter())
                .body(registerRequest)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .extract().as(RegisterResponse.class);

        UniversalComparator.match(registerRequest, registerResponse);

        softly.assertThat(registerResponse.getUserId()).isInstanceOf(Long.class);
        softly.assertThat(registerResponse.getEmail()).isEqualTo(registerRequest.getEmail());
        softly.assertThat(registerResponse.getRole()).isEqualTo(Role.USER.toString());

//        Login as admin
        LoginResponse loginResponse = given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new CustomLoggingFilter())
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

        String accessToken = loginResponse.getAccessToken();

//        Check if user exists thru admin request
        List<AdminUserResponse> users = given()
                .baseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .filter(new CustomLoggingFilter())
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<AdminUserResponse>>() {
                });

        Boolean isUserExist = users.stream().anyMatch(user -> user.getNickname().equals(registerRequest.getNickname())
                && user.getEmail().equals(registerRequest.getEmail()));
        softly.assertThat(isUserExist).isTrue();

//        Check if user exists in database directly using JDBC
        try (Connection connection = getConnection()) {
            String sql = "SELECT id, nickname, email, password_hash, role, created_at FROM users WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, registerRequest.getEmail());
                try (ResultSet resultSet = statement.executeQuery()) {
                    softly.assertThat(resultSet.next()).isTrue();
                    softly.assertThat(resultSet.getLong("id")).isInstanceOf(Long.class);
                    softly.assertThat(resultSet.getString("nickname")).isEqualTo(registerRequest.getNickname());
                    softly.assertThat(resultSet.getString("email")).isEqualTo(registerRequest.getEmail());
                    softly.assertThat(resultSet.getString("password_hash")).isNotNull();
                    softly.assertThat(resultSet.getString("role")).isEqualTo(Role.USER.toString());
                    softly.assertThat(resultSet.getTimestamp("created_at")).isNotNull();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user in database", e);
        }
    }
}
