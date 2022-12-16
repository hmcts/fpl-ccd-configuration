package uk.gov.hmcts.reform.fpl.api;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.service.AuthenticationService;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.EmailService;
import uk.gov.hmcts.reform.fpl.service.PaymentService;
import uk.gov.hmcts.reform.fpl.service.ScenarioService;
import uk.gov.hmcts.reform.fpl.util.ObjectMapperApiTestConfig;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

@ActiveProfiles("test-${env:local}")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {
    ApiTestService.class,
    AuthenticationService.class,
    CaseService.class,
    DocumentService.class,
    EmailService.class,
    PaymentService.class,
    ScenarioService.class,
    TestConfiguration.class,
    ObjectMapperApiTestConfig.class,
    CaseConverter.class
})
public abstract class AbstractApiTest {

    @BeforeEach
    public void setUp(@Autowired TestConfiguration testConfiguration) {
        RestAssured.baseURI = testConfiguration.getFplUrl();
        RestAssured.useRelaxedHTTPSValidation();
    }

}
