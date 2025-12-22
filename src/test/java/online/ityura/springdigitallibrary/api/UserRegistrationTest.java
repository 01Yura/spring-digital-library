package online.ityura.springdigitallibrary.api;

import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;


public class UserRegistrationTest extends BaseApiTest {

    @Test
    void userCanLoginWithValidData() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .baseUri(Config.getProperty("apiBaseUrl"))
                .body("""
                        {
                          "nickname": "TestUser1",
                          "email": "TestUser1@example.com",
                          "password": "TestUser1!"
                        }
                        """)
                .when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(201);
    }
}
