package online.ityura.springdigitallibrary.api;

import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


public class UserRegistrationTest extends BaseApiTest {

    @Test
    void userCanLoginWithValidData() {
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
    }
}
