package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.Map;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
@SuppressWarnings("unchecked")
class RepresentativeAboutToSubmitEventControllerTest extends AbstractControllerTest {

    private final String serviceAuthToken = RandomStringUtils.randomAlphanumeric(10);
    private final String representativeEmail = "test@test.com";

    private final Representative.RepresentativeBuilder representativeBuilder = Representative.builder()
        .fullName("John Smith")
        .positionInACase("Position")
        .role(RepresentativeRole.REPRESENTING_PERSON_1)
        .servingPreferences(DIGITAL_SERVICE);


    public RepresentativeAboutToSubmitEventControllerTest() {
        super("manage-representatives");
    }

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseUserApi caseUserApi;

    @Test
    void shouldSuccessfullyValidateRepresentativeAccountExistence() {
        final Long caseId = RandomUtils.nextLong();
        final String userId = RandomStringUtils.randomAlphanumeric(10);

        Map<String, Object> incomingCaseDate = caseDataWithRepresentatives(representativeBuilder
            .email(representativeEmail)
            .role(RepresentativeRole.REPRESENTING_PERSON_1)
            .build());

        given(authTokenGenerator.generate()).willReturn(serviceAuthToken);
        given(organisationApi.findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail))
            .willReturn(new User(userId));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postSubmittedEvent(caseId, incomingCaseDate, SC_OK);

        verify(organisationApi).findUsersByEmail(userAuthToken, serviceAuthToken, representativeEmail);

        verify(caseUserApi).updateCaseRolesForUser(userAuthToken, serviceAuthToken,
            caseId.toString(), userId, new CaseUser(userId, Set.of("[SOLICITOR]")));

        assertThat(callbackResponse.getErrors()).isNullOrEmpty();

    }

    Map<String, Object> caseDataWithRepresentatives(Representative... representatives) {
        return ImmutableMap.of("representatives", ElementUtils.wrap(representatives));
    }

}
