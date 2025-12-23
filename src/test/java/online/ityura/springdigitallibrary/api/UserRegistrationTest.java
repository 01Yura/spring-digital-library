package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


public class UserRegistrationTest extends BaseApiTest {

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
                          "nickname": "TestUser6",
                          "email": "TestUser6@example.com",
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
        String refreshToken = response.getRefreshToken();

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

        Boolean isUserExist = users.stream().anyMatch(user -> user.getNickname().equals("TestUser6")
        && user.getEmail().equals("TestUser6@example.com"));
        softly.assertThat(isUserExist).isTrue();
    }


}
