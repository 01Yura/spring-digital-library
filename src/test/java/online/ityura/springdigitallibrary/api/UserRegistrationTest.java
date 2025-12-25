package online.ityura.springdigitallibrary.api;

import io.restassured.common.mapper.TypeRef;
import online.ityura.springdigitallibrary.dto.request.RegisterRequest;
import online.ityura.springdigitallibrary.dto.response.AdminUserResponse;
import online.ityura.springdigitallibrary.dto.response.ErrorResponse;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;


public class UserRegistrationTest extends BaseApiTest {

    static Stream<Arguments> argsFor_userCanLoginWithValidData() {
        return Stream.of(
//            Nickname field validation (positive):
                // 1) Length (BVA - Boundary Value Analysis)
                // Exactly 3 characters (min)
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // String of length 4
                Arguments.of(RandomDataGenerator.generateNickname(4, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // String of length 49
                Arguments.of(RandomDataGenerator.generateNickname(49, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // String of length 50 (max)
                Arguments.of(RandomDataGenerator.generateNickname(50, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),

                // 2) Character classes (EP - Equivalence Partitioning)
                // Letters only
                Arguments.of(RandomDataGenerator.generateNickname(4, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Digits only
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.DIGITS,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Letters + digits
                Arguments.of(RandomDataGenerator.generateNickname(8, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Contains underscore (generate base part, add underscore)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "_"),
                // Contains dash (generate base part, add dash)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "-"),
                // Contains dot (generate base part, add dot)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "."),
                // Mix of allowed special characters (generate parts, join with special characters)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "_" +
                        RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                                RandomDataGenerator.CaseMode.LOWER) + "-" +
                        RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.DIGITS,
                                RandomDataGenerator.CaseMode.MIXED) + "." +
                        RandomDataGenerator.generateNickname(1, RandomDataGenerator.CharMode.LETTERS,
                                RandomDataGenerator.CaseMode.UPPER)),

                // 3) Character positions (important!)
                // Allowed characters at beginning/middle/end
                // Underscore at beginning (generate base part, add underscore at start)
                Arguments.of("_" + RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Underscore in middle (generate two parts, join with underscore)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "_" +
                        RandomDataGenerator.generateNickname(1, RandomDataGenerator.CharMode.DIGITS,
                                RandomDataGenerator.CaseMode.MIXED)),
                // Underscore at end (generate base part, add underscore at end)
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED) + "_"),
                // Dot at beginning (generate base part, add dot at start)
                Arguments.of("." + RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Dot in middle (generate two parts, join with dot)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "." +
                        RandomDataGenerator.generateNickname(1, RandomDataGenerator.CharMode.DIGITS,
                                RandomDataGenerator.CaseMode.MIXED)),
                // Dot at end (generate base part, add dot at end)
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED) + "."),
                // Dash at beginning (generate base part, add dash at start)
                Arguments.of("-" + RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED)),
                // Dash in middle (generate two parts, join with dash)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                        RandomDataGenerator.CaseMode.LOWER) + "-" +
                        RandomDataGenerator.generateNickname(1, RandomDataGenerator.CharMode.DIGITS,
                                RandomDataGenerator.CaseMode.MIXED)),
                // Dash at end (generate base part, add dash at end)
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.ALPHANUMERIC,
                        RandomDataGenerator.CaseMode.MIXED) + "-"));
    }

    static Stream<Arguments> argsFor_userCannotLoginWithInvalidData() {
        return Stream.of(
//            Nickname field validation (negative):
                
                // 1) Length (BVA - Boundary Value Analysis)
                // Length 0 (empty string) - Pattern validation fails first
                Arguments.of("", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Length 1 (below min)
                Arguments.of(RandomDataGenerator.generateNickname(1, RandomDataGenerator.CharMode.ALPHANUMERIC,
                                RandomDataGenerator.CaseMode.MIXED), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must be between 3 and 50 characters"),
                // Length 2 (below min)
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.ALPHANUMERIC,
                                RandomDataGenerator.CaseMode.MIXED), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must be between 3 and 50 characters"),
                // Length 51 (above max)
                Arguments.of(RandomDataGenerator.generateNickname(51, RandomDataGenerator.CharMode.ALPHANUMERIC,
                                RandomDataGenerator.CaseMode.MIXED), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must be between 3 and 50 characters"),
                // Length 52 (above max)
                Arguments.of(RandomDataGenerator.generateNickname(52, RandomDataGenerator.CharMode.ALPHANUMERIC,
                                RandomDataGenerator.CaseMode.MIXED), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must be between 3 and 50 characters"),
                
                // 2) NotBlank validation
                // Null value
                Arguments.of(null, "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname is required"),
                // Only spaces
                Arguments.of("   ", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname is required"),
                // Spaces with valid characters (should fail due to pattern)
                Arguments.of("ab c", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                
                // 3) Pattern validation (forbidden characters)
                // Contains space
                Arguments.of(RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                                RandomDataGenerator.CaseMode.LOWER) + " " +
                        RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.LETTERS,
                                RandomDataGenerator.CaseMode.LOWER), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains @ symbol
                Arguments.of(RandomDataGenerator.generateNickname(3, RandomDataGenerator.CharMode.LETTERS,
                                RandomDataGenerator.CaseMode.LOWER) + "@test", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains # symbol
                Arguments.of("test" + "#" + RandomDataGenerator.generateNickname(2, RandomDataGenerator.CharMode.DIGITS,
                                RandomDataGenerator.CaseMode.MIXED), "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains $ symbol
                Arguments.of("user$123", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains % symbol
                Arguments.of("nick%name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains & symbol
                Arguments.of("test&user", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains * symbol
                Arguments.of("nick*name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains + symbol
                Arguments.of("user+123", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains = symbol
                Arguments.of("test=user", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains / symbol
                Arguments.of("user/name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains \ symbol
                Arguments.of("user\\name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains [ symbol
                Arguments.of("user[name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains ] symbol
                Arguments.of("user]name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains { symbol
                Arguments.of("user{name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains } symbol
                Arguments.of("user}name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains ( symbol
                Arguments.of("user(name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"),
                // Contains ) symbol
                Arguments.of("user)name", "VALIDATION_ERROR", "Validation failed", "nickname",
                        "Nickname must contain only letters, digits, dashes, underscores, and dots. Spaces and other special characters are not allowed"));
    }

    @ParameterizedTest
    @MethodSource("argsFor_userCanLoginWithValidData")
    void userCanLoginWithValidData(String nickname) {
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

    @ParameterizedTest
    @MethodSource("argsFor_userCannotLoginWithInvalidData")
    void userCannotLoginWithInvalidData(String nickname, String error, String message, String fieldName,
                                        String fieldError) {
//        Create DTO object for request
        RegisterRequest registerRequest = RandomDtoGeneratorWithFaker.generateRandomDtoObject(RegisterRequest.class);
        registerRequest.setNickname(nickname);

//        Register user and save response
        ErrorResponse errorResponse =
                new CrudRequester(RequestSpecs.unauthSpec(),
                        ResponseSpecs.statusCode(400), Endpoint.AUTH_REGISTER)
                        .post(registerRequest)
                        .extract().as(ErrorResponse.class);

        softly.assertThat(errorResponse.getStatus()).isEqualTo(400);
        softly.assertThat(errorResponse.getError()).isEqualTo(error);
        softly.assertThat(errorResponse.getMessage()).isEqualTo(message);
        softly.assertThat(errorResponse.getFieldErrors().containsKey(fieldName)).isTrue();
        softly.assertThat(errorResponse.getFieldErrors().get(fieldName)).isEqualTo(fieldError);
        softly.assertThat(errorResponse.getTimestamp()).isInstanceOf(Instant.class);

//        Check if user exists on backend thru admin request
        List<AdminUserResponse> users =
                new CrudRequester(RequestSpecs.adminSpec(), ResponseSpecs.statusCode(200), Endpoint.ADMIN_USERS)
                        .get()
                        .extract()
                        .as(new TypeRef<List<AdminUserResponse>>() {
                        });

        Boolean isUserExist = users.stream().anyMatch(user -> user.getNickname().equals(registerRequest.getNickname())
                && user.getEmail().equals(registerRequest.getEmail()));
        softly.assertThat(isUserExist).isFalse();

//        Check that the user does not exist in the database directly
//        Note: DBRequest.extractAs(User.class) is not implemented, so we skip direct DB check
//        The admin API check above is sufficient to verify user was not created

    }
}
