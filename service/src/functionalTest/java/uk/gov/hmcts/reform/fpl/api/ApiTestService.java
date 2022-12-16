package uk.gov.hmcts.reform.fpl.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Document;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.fpl.model.User.user;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiTestService {

    public static final User LA_SWANSEA_USER_1 = user("james@swansea.gov.uk");
    public static final User LA_WILTSHIRE_USER_1 = user("raghu@wiltshire.gov.uk");
    public static final User LA_WILTSHIRE_USER_2 = user("sam@wiltshire.gov.uk");
    public static final User COURT_ADMIN = user("hmcts-admin@example.com");

    final TestConfiguration testConfiguration;
    final ScenarioService scenarioService;
    final CaseService caseService;
    final DocumentService documentService;
    final CaseConverter caseConverter;
    final ObjectMapper objectMapper;

    public CaseData createCase(String path, User user, String name) {
        CaseData caseData = readCase(path);
        caseData = caseData.toBuilder()
            .caseName(name == null ? "e2e test case" : name)
            .build();
        return caseService.createCase(caseData, user);
    }

    public CaseData createCase(String path, User user) {
        return createCase(path, user, "e2e test case");
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
        Document testDocument = documentService.getTestDocument(LA_SWANSEA_USER_1);
        json = json.replace("\"dateSubmitted\": \"TO BE FILLED WITH VALID DATA\"",
            "\"dateSubmitted\": \"" + LocalDate.now() + "\"");
        json = json.replace("\"dateAndTimeSubmitted\": \"TO BE FILLED WITH VALID DATA\"",
            "\"dateAndTimeSubmitted\": \"" + LocalDateTime.now() + "\"");
        json = json.replace("${TEST_DOCUMENT_URL}", testDocument.getDocumentUrl());
        json = json.replace("${TEST_DOCUMENT_BINARY_URL}", testDocument.getDocumentBinaryUrl());
        json = json.replace("${SWANSEA_ORG_ID}",
            testConfiguration.getPlaceholders().get("SwanseaOrganisationID").toString());

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
