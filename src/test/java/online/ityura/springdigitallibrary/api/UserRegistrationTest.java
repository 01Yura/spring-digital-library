package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.testinfra.comparators.UniversalComparator;
import online.ityura.springdigitallibrary.testinfra.database.Condition;
import online.ityura.springdigitallibrary.testinfra.database.DBRequest;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDtoGeneratorWithFaker;
import online.ityura.springdigitallibrary.testinfra.requests.clients.CrudRequester;
import online.ityura.springdigitallibrary.testinfra.requests.clients.Endpoint;
import online.ityura.springdigitallibrary.testinfra.specs.RequestSpecs;
import online.ityura.springdigitallibrary.testinfra.specs.ResponseSpecs;
import org.junit.jupiter.api.Test;

import java.util.List;


public class UserRegistrationTest extends BaseApiTest {

    @Test
    void userCanLoginWithValidData() {
//        Create DTO object for request
        RegisterRequest registerRequest = RandomDtoGeneratorWithFaker.generateRandomDtoObject(RegisterRequest.class);

//        Register user and save response
        RegisterResponse registerResponse =
                new CrudRequester(RequestSpecs.unauthSpec(),
                        ResponseSpecs.statusCode(201), Endpoint.AUTH_REGISTER)
                        .post(registerRequest)
                        .extract().as(RegisterResponse.class);

        // Check that the response meets the request and valid
        UniversalComparator.match(registerRequest, registerResponse);

//        Check if user exists on backend thru admin request
        List<AdminUserResponse> users =
                new CrudRequester(RequestSpecs.adminSpec(), ResponseSpecs.statusCode(200), Endpoint.ADMIN_USERS)
                        .get()
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
