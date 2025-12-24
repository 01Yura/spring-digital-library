package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.Role;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.testinfra.comparators.UniversalComparator;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.database.Condition;
import online.ityura.springdigitallibrary.testinfra.database.DBRequest;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDtoGeneratorWithFaker;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;


public class UserRegistrationTest extends BaseApiTest {

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

        // Проверка соответствия полей между request и response
        UniversalComparator.match(registerRequest, registerResponse);

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

//        Check if user exists in database directly using DBRequest
        User user = DBRequest.builder()
                .requestType(DBRequest.RequestType.SELECT)
                .table("users")
                .where(Condition.equalTo("email", registerRequest.getEmail()))
                .extractAs(User.class);

        softly.assertThat(user).isNotNull();
        softly.assertThat(user.getId()).isInstanceOf(Long.class);
        softly.assertThat(user.getNickname()).isEqualTo(registerRequest.getNickname());
        softly.assertThat(user.getEmail()).isEqualTo(registerRequest.getEmail());
        softly.assertThat(user.getPasswordHash()).isNotNull();
        softly.assertThat(user.getRole()).isEqualTo(Role.USER);
        softly.assertThat(user.getCreatedAt()).isNotNull();
    }
}
