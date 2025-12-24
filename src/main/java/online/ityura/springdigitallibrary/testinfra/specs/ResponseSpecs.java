package online.ityura.springdigitallibrary.testinfra.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;

public class ResponseSpecs {
    private ResponseSpecs() {
    }

    private static ResponseSpecBuilder defaultResponseSpecBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification responseReturns200Spec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(200)
                .build();
    }

    public static ResponseSpecification responseReturns201Spec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(201)
                .build();
    }


    public static ResponseSpecification responseReturns400Spec() {
        return defaultResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }
}
