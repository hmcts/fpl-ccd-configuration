package uk.gov.hmcts.reform.fpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.Scenario;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.util.AuthenticationService;
import uk.gov.hmcts.reform.fpl.util.CaseService;
import uk.gov.hmcts.reform.fpl.util.ScenarioService;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import static com.gargoylesoftware.htmlunit.util.MimeType.APPLICATION_JSON;
import static io.restassured.http.Headers.headers;
import static java.lang.String.format;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.hmcts.reform.fpl.util.CallbackComparator.callbackComparator;
import static uk.gov.hmcts.reform.fpl.util.StringUtils.blue;
import static uk.gov.hmcts.reform.fpl.util.StringUtils.red;


@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("test-${env:local}")
@ContextConfiguration(classes = {
    TestConfiguration.class,
    ObjectMapper.class,
    ScenarioService.class,
    AuthenticationService.class,
    CaseService.class,
    CaseConverter.class
})
public class CallbackTest {

    @Autowired
    private TestConfiguration testConfiguration;

    @Autowired
    private ScenarioService scenarioService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CaseService caseService;

    @Before
    public void setUp() {
        RestAssured.baseURI = testConfiguration.getFplUrl();
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void callbackShouldReturnExpectedResponse() {

        for (Scenario scenario : scenarioService.getScenarios()) {
            System.out.println(blue(format("%s (%s)", scenario.getName(), scenario.getDescription())));

            String actualResponseBody = SerenityRest
                .given()
                .headers(getAuthorizationHeaders(scenario))
                .contentType(APPLICATION_JSON)
                .body(scenario.getRequest().getDataAsString())
                .when()
                .post(scenario.getRequest().getUri())
                .then()
                .statusCode(scenario.getExpectation().getStatus())
                .and()
                .extract()
                .body()
                .asString();

            assertResponse(scenario, actualResponseBody);
        }
    }

    private void assertResponse(Scenario scenario, String response) {
        try {
            assertEquals(scenario.getExpectation().getDataAsString(), response, callbackComparator());
        } catch (AssertionError assertionError) {
            System.out.println("Expected:");
            System.out.println(red(scenario.getExpectation().getDataAsString()));
            System.out.println("Actual:");
            System.out.println(red(response));
            throw assertionError;
        }
    }

    private Headers getAuthorizationHeaders(Scenario scenario) {
        User user = testConfiguration.getUsers().get(scenario.getRequest().getUser());
        return headers(new Header("Authorization", authenticationService.getAccessToken(user)));
    }
}
