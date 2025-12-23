package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.LoginResponse;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.repository.UserRepository;
import online.ityura.springdigitallibrary.testinfra.configs.Config;
import online.ityura.springdigitallibrary.testinfra.helper.CustomLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserRegistrationWithHibernateTest extends BaseApiTest {

    @Autowired
    private UserRepository userRepository;

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

//        Check if user exists in database directly
        Optional<User> userFromDb = userRepository.findByEmail("TestUser1@example.com");
        softly.assertThat(userFromDb).isPresent();
        softly.assertThat(userFromDb.get().getId()).isInstanceOf(Long.class);
        softly.assertThat(userFromDb.get().getNickname()).isEqualTo("TestUser1");
        softly.assertThat(userFromDb.get().getEmail()).isEqualTo("TestUser1@example.com");
        softly.assertThat(userFromDb.get().getPasswordHash()).isNotNull();
        softly.assertThat(userFromDb.get().getRole()).isEqualTo(User.Role.USER);
    }


}
