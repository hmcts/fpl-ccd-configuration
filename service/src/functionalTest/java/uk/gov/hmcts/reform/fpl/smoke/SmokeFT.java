package uk.gov.hmcts.reform.fpl.smoke;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
public class SmokeFT {

    @Value("${test-conf.fpl-url:http://localhost:4013}")
    private String testUrl;

    @Test
    public void testHealthEndpoint() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .baseUri(testUrl)
                .get("/health")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
    }
}