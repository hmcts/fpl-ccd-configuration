package uk.gov.hmcts.reform.fpl.controllers;

import feign.FeignException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalRepresentativeMidEventControllerTest extends AbstractControllerTest {

    public static final String SERVICE_AUTH_TOKEN = RandomStringUtils.randomAlphanumeric(10);
    public static final String REPRESENTATIVE_EMAIL = "test@test.com";
    public static final String REP_NAME = "John Smith";
    public static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName(REP_NAME)
        .role(LegalRepresentativeRole.EXTERNAL_LA_BARRISTER)
        .email(REPRESENTATIVE_EMAIL)
        .organisation("organisation")
        .telephoneNumber("07500045455")
        .build();
    private static final Long CASE_ID = 12345L;
    private static final String USER_ID = RandomStringUtils.randomAlphanumeric(10);
    private static final String USER_ID_2 = RandomStringUtils.randomAlphanumeric(10);

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationApi organisationApi;

    ManageLegalRepresentativeMidEventControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldReturnValidationErrorForNonExistingUser() {

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL)).willThrow(
            mock(FeignException.NotFound.class));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actual = postMidEvent(callbackRequest);

        assertThat(actual.getErrors()).containsOnly(
            "Email address for Legal representative is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register"
        );
    }

    @Test
    void shouldValidateAnExistingUser() {

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL))
            .willReturn(new OrganisationUser(USER_ID));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actual = postMidEvent(callbackRequest);

        assertThat(actual.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorsWhenLALegalRepresentativeEmailsAreInvalid() {
        CaseData caseData = CaseData.builder()
            .legalRepresentatives(wrapElements(LegalRepresentative.builder()
                    .email("Test user <Test.User@HMCTS.NET>")
                    .build(), LegalRepresentative.builder()
                .email("test@test.com")
                    .build(),
                LegalRepresentative.builder()
                    .email("Test user <Test.User@HMCTS.NET>")
                    .build())).build();

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "Test user <Test.User@HMCTS.NET>"))
            .willReturn(new OrganisationUser(USER_ID));
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "test@test.com"))
            .willReturn(new OrganisationUser(USER_ID));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "LA Legal Representative 1: Enter an email address in the correct format, for example name@example.com",
            "LA Legal Representative 3: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnErrorsWhenLALegalRepresentativeEmailIsValid() {
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL))
            .willReturn(new OrganisationUser(USER_ID));

        AboutToStartOrSubmitCallbackResponse actual = postMidEvent(caseDetails);

        assertThat(actual.getErrors()).isNull();
    }

    private CallbackRequest buildCallbackRequest(CaseDetails originalCaseDetails, CaseDetails caseDetails) {
        return CallbackRequest.builder()
            .caseDetailsBefore(originalCaseDetails)
            .caseDetails(caseDetails)
            .build();
    }

    private CaseDetails buildCaseData(List<Element<LegalRepresentative>> legalRepresentatives) {
        return CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of(
                "legalRepresentatives", legalRepresentatives
            ))
            .build();
    }
}
