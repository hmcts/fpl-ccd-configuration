package uk.gov.hmcts.reform.fpl.controllers;

import feign.FeignException;
import feign.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class RepresentativeMidEventControllerTest extends AbstractControllerTest {

    private final String serviceAuthToken = RandomStringUtils.randomAlphanumeric(10);
    private final String representativeEmail = "test@test.com";

    private final Representative.RepresentativeBuilder representativeBuilder = Representative.builder()
        .fullName("John Smith")
        .positionInACase("Position")
        .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
        .servingPreferences(DIGITAL_SERVICE);

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationApi organisationApi;

    RepresentativeMidEventControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldValidateRepresentativesAndReturnValidationErrors() {
        CaseDetails caseDetails = buildCaseDetails(representativeBuilder.build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).contains("Enter an email address for Representative");
    }

    @Test
    void shouldValidateRepresentativeAccountExistenceAndReturnValidationErrors() {
        CaseDetails caseDetails = buildCaseDetails(representativeBuilder
            .email(representativeEmail).build());

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findUserByEmail(userAuthToken, serviceAuthToken, representativeEmail))
            .willThrow(new FeignException.NotFound("User not found",
                Request.create(GET, "", Map.of(), new byte[] {}, UTF_8),
                new byte[] {}));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        verify(organisationApi).findUserByEmail(userAuthToken, serviceAuthToken, representativeEmail);

        assertThat(callbackResponse.getErrors())
            .contains("Representative must already have an account with the digital service");
    }

    @Test
    void shouldSuccessfullyValidateRepresentativeAccountExistence() {
        CaseDetails caseDetails = buildCaseDetails(representativeBuilder
            .email(representativeEmail).build());

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findUserByEmail(userAuthToken, serviceAuthToken, representativeEmail))
            .willReturn(new User(RandomStringUtils.randomAlphanumeric(10)));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        verify(organisationApi).findUserByEmail(userAuthToken, serviceAuthToken, representativeEmail);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    CaseDetails buildCaseDetails(Representative... representatives) {
        return CaseDetails.builder()
            .id(RandomUtils.nextLong())
            .data(Map.of(
                "representatives", wrapElements(representatives),
                "respondents1", wrapElements(Respondent.builder().build())))
            .build();
    }
}
