package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.testinfra.comparators.UniversalComparator;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.database.Condition;
import online.ityura.springdigitallibrary.testinfra.database.DBRequest;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDtoGeneratorWithFaker;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import online.ityura.springdigitallibrary.testinfra.specs.RequestSpecs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;


public class UserRegistrationTest extends BaseApiTest {

    @Test
    void userCanLoginWithValidData() {
//        Create DTO object for request
        RegisterRequest registerRequest = RandomDtoGeneratorWithFaker.generateRandomDtoObject(RegisterRequest.class);

//        Register user ans save response
        RegisterResponse registerResponse = given()
                .spec(RequestSpecs.unauthSpec())
                .body(registerRequest)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath(
                        "contracts/api/v1/auth/register/post-response.schema.json"
                ))
                .extract().as(RegisterResponse.class);

        // Check that response meet the request and valid
        UniversalComparator.match(registerRequest, registerResponse);

//        Check if user exists on backend thru admin request
        List<AdminUserResponse> users = given()
                .spec(RequestSpecs.adminSpec())
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("contracts/api/v1/admin/users/get-response.schema.json"))
                .extract()
                .as(new TypeRef<List<AdminUserResponse>>() {
                });

        Boolean isUserExist = users.stream().anyMatch(user -> user.getNickname().equals(registerRequest.getNickname())
                && user.getEmail().equals(registerRequest.getEmail()));
        softly.assertThat(isUserExist).isTrue();

//        Check if the user exists in the database directly
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
        softly.assertThat(user.getRole()).isEqualTo(registerResponse.getRole());
        softly.assertThat(user.getCreatedAt()).isNotNull();
    }
}
