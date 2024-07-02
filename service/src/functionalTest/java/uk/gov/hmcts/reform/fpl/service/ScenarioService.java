package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Scenario;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static uk.gov.hmcts.reform.fpl.util.CallbackComparator.callbackComparator;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public final class ScenarioService {

    private final ObjectMapper objectMapper;
    private final TestConfiguration testConfiguration;
    private final AuthenticationService authenticationService;

    public Scenario getScenario(String path) {
        try {
            String scenarioString = readString(path, testConfiguration.getPlaceholders());
            return objectMapper.readValue(scenarioString, Scenario.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String executeScenario(Scenario scenario) {
        return SerenityRest
            .given()
            .headers(authenticationService.getAuthorizationHeaders(testConfiguration.getUsers()
                .get(scenario.getRequest().getUser())))
            .contentType(ContentType.JSON)
            .body(scenario.getRequest().getDataAsString())
            .post(scenario.getRequest().getUri())
            .then()
            .statusCode(scenario.getExpectation().getStatus())
            .extract()
            .asString();
    }

    public void assertScenario(Scenario scenario, String response) {
        String expectedData = scenario.getExpectation().getDataAsString();
        if (isNotEmpty(expectedData)) {
            String actualData = isEmpty(response) ? "{}" : response;
            assertEquals(expectedData, actualData, callbackComparator());
        }
    }
}
