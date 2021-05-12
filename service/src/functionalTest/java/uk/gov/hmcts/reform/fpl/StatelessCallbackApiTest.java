package uk.gov.hmcts.reform.fpl;

import io.restassured.http.Headers;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.Scenario;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.service.ScenarioService;

import static com.gargoylesoftware.htmlunit.util.MimeType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.hmcts.reform.fpl.util.CallbackComparator.callbackComparator;
import static uk.gov.hmcts.reform.fpl.util.StringUtils.blue;
import static uk.gov.hmcts.reform.fpl.util.StringUtils.red;


public class StatelessCallbackApiTest extends AbstractApiTest {

    @Autowired
    private ScenarioService scenarioService;

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
            String expectedData = scenario.getExpectation().getDataAsString();
            if (isNotEmpty(expectedData)) {
                String actualData = isEmpty(response) ? "{}" : response;
                assertEquals(expectedData, actualData, callbackComparator());
            }
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
        return authenticationService.getAuthorizationHeaders(user);
    }
}
