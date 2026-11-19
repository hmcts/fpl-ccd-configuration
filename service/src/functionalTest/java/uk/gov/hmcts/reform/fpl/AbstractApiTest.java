package uk.gov.hmcts.reform.fpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Scenario;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.service.AuthenticationService;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.fpl.service.PaymentService;
import uk.gov.hmcts.reform.fpl.service.ScenarioService;
import uk.gov.hmcts.reform.fpl.util.ObjectMapperApiTestConfig;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;

import static uk.gov.hmcts.reform.fpl.model.User.user;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@ActiveProfiles("test-${env:local}")
@ContextConfiguration(classes = {
    TestConfiguration.class,
    CaseConverter.class,
    ObjectMapperApiTestConfig.class,
    AuthenticationService.class,
    ScenarioService.class,
    CaseService.class,
    DocumentService.class,
    PaymentService.class,
    EmailService.class
})
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@Slf4j
public abstract class AbstractApiTest {

    static final User LA_SWANSEA_USER_1 = user("local-authority-swansea-0001@maildrop.cc");
    static final User LA_WILTSHIRE_USER_1 = user("raghu@wiltshire.gov.uk");
    static final User LA_WILTSHIRE_USER_2 = user("sam@wiltshire.gov.uk");
    static final User COURT_ADMIN = user("hmcts-admin@example.com");

    @Autowired
    protected TestConfiguration testConfiguration;

    @Autowired
    protected ScenarioService scenarioService;

    @Autowired
    protected CaseService caseService;

    @Autowired
    protected CaseConverter caseConverter;

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void setUp() {
        RestAssured.baseURI = testConfiguration.getFplUrl();
        log.info("api test url: " + testConfiguration.getFplUrl());
        RestAssured.useRelaxedHTTPSValidation();
    }

    CaseData createCase(String path, User user) {
        CaseData caseData = readCase(path);
        return caseService.createCase(caseData, user);
    }

    CallbackResponse callback(CaseData caseDetails, User user, String callback) {
        return caseService.callback(caseDetails, user, "/callback/" + callback);
    }

    void submittedCallback(CaseData caseDetails, User user, String callback) {
        submittedCallback(caseDetails, caseDetails, user, callback);
    }

    void submittedCallback(CaseData caseDetails, CaseData caseDetailsBefore, User user, String callback) {
        caseService.submittedCallback(caseDetails, caseDetailsBefore, user, "/callback/" + callback);
    }

    private CaseData readCase(String path) {
        String json = readString(path);

        try {
            return objectMapper.readValue(json, CaseData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void verifyScenario(String scenarioPath) {
        Scenario scenario = scenarioService.getScenario(scenarioPath);
        String response = scenarioService.executeScenario(scenario);
        scenarioService.assertScenario(scenario, response);
    }

    public Object configValue(String property) {
        return testConfiguration.getPlaceholders().get(property);
    }

}
