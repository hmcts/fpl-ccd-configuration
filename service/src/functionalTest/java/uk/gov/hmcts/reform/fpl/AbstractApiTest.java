package uk.gov.hmcts.reform.fpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.util.AuthenticationService;
import uk.gov.hmcts.reform.fpl.util.CaseService;
import uk.gov.hmcts.reform.fpl.util.DocumentService;
import uk.gov.hmcts.reform.fpl.util.EmailService;
import uk.gov.hmcts.reform.fpl.util.PaymentService;
import uk.gov.hmcts.reform.fpl.util.TestConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;


@ActiveProfiles("test-${env:preview}")
@ContextConfiguration(classes = {
    TestConfiguration.class,
    AuthenticationService.class,
    CaseService.class,
    DocumentService.class,
    CaseConverter.class,
    ObjectMapperConfig.class,
    PaymentService.class,
    EmailService.class,
})
public abstract class AbstractApiTest {

    @Autowired
    protected TestConfiguration testConfiguration;

    @Autowired
    protected CaseService caseService;

    @Autowired
    protected CaseConverter caseConverter;

    @Autowired
    protected ObjectMapper objectMapper;

    @Before
    public void setUp() {
        RestAssured.baseURI = testConfiguration.getFplUrl();
        RestAssured.useRelaxedHTTPSValidation();
    }

    CaseData createCase(String path, User user) {
        CaseData caseData = readCase(path);
        return caseService.createCase(caseData, user);
    }

    CallbackResponse callback(CaseData caseDetails, User user, String callback) {
        return caseService.callback(caseDetails, user, "/callback/" + callback);
    }

    private CaseData readCase(String path) {
        String json = readString(path);

        try {
            return objectMapper.readValue(json, CaseData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void assertEqualsNormalizingSpaces(String actual, String expected) {
        assertThat(normalizeSpace(actual)).isEqualTo(normalizeSpace(expected));
    }
}
