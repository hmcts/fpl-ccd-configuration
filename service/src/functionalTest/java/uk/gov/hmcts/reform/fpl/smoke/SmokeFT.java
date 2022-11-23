package uk.gov.hmcts.reform.fpl.smoke;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

@ActiveProfiles("test-${env:local}")
@SpringBootTest
@ContextConfiguration(classes = {
    TestConfiguration.class
})
@RunWith(SpringIntegrationSerenityRunner.class)
public class SmokeFT {

    @Autowired
    protected TestConfiguration testConfiguration;

    @Test
    public void testHealthEndpoint() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured
                .given()
                .baseUri(testConfiguration.getFplUrl())
                .get("/health")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
    }
}
