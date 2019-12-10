package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
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

    public RepresentativeMidEventControllerTest() {
        super("manage-representatives");
    }

    @Test
    void shouldValidateRepresentativesAndReturnValidationErrors() {
        Map<String, Object> incomingCaseDate = caseDataWithRepresentatives(representativeBuilder.build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(incomingCaseDate);

        assertThat(callbackResponse.getErrors()).contains("Enter an email address for Representative");
    }

    @Test
    void shouldValidateRepresentativeAccountExistenceAndReturnValidationErrors() {
        Map<String, Object> incomingCaseDate = caseDataWithRepresentatives(representativeBuilder
            .email(representativeEmail).build());

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail))
            .willThrow(new FeignException.NotFound("User not found", null));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(incomingCaseDate);

        verify(organisationApi).findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail);

        assertThat(callbackResponse.getErrors())
            .contains("Representative must already have an account with the digital service");
    }

    @Test
    void shouldSuccessfullyValidateRepresentativeAccountExistence() {
        Map<String, Object> incomingCaseDate = caseDataWithRepresentatives(representativeBuilder
            .email(representativeEmail).build());

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail))
            .willReturn(new User(RandomStringUtils.randomAlphanumeric(10)));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(incomingCaseDate);

        verify(organisationApi).findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    Map<String, Object> caseDataWithRepresentatives(Representative... representatives) {
        return ImmutableMap.of("representatives", ElementUtils.wrap(representatives), "respondents1", ElementUtils.wrap(Respondent.builder().build()));
    }
}
