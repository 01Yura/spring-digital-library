package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.RegisterResponse;
import online.ityura.springdigitallibrary.model.User;
import online.ityura.springdigitallibrary.testinfra.comparators.UniversalComparator;
import online.ityura.springdigitallibrary.testinfra.database.Condition;
import online.ityura.springdigitallibrary.testinfra.database.DBRequest;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDataGenerator;
import online.ityura.springdigitallibrary.testinfra.generators.RandomDtoGeneratorWithFaker;
import online.ityura.springdigitallibrary.testinfra.requests.clients.CrudRequester;
import online.ityura.springdigitallibrary.testinfra.requests.clients.Endpoint;
import online.ityura.springdigitallibrary.testinfra.specs.RequestSpecs;
import online.ityura.springdigitallibrary.testinfra.specs.ResponseSpecs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;


public class UserRegistrationTest extends BaseApiTest {

    static Stream<Arguments> argsFor_userCanLoginWithValidData() {
        return Stream.of(
//            Nickname field validation (positive):
                // min length 3
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.MIXED)),
                // max length 50
                Arguments.of(RandomDataGenerator.generateNickname(50, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.MIXED)));
    }

        @ParameterizedTest
        @MethodSource("argsFor_userCanLoginWithValidData")
        void userCanLoginWithValidData(String nickname){
//        Create DTO object for request
            RegisterRequest registerRequest = RandomDtoGeneratorWithFaker.generateRandomDtoObject(RegisterRequest.class);
            registerRequest.setNickname(nickname);

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
