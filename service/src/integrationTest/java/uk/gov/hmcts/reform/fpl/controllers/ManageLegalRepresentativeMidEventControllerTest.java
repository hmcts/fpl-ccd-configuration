package uk.gov.hmcts.reform.fpl.controllers;

import feign.FeignException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalRepresentativeMidEventControllerTest extends AbstractCallbackTest {

    private static final String REPRESENTATIVE_EMAIL = "test@test.com";
    private static final String REP_NAME = "John Smith";
    private static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName(REP_NAME)
        .role(LegalRepresentativeRole.EXTERNAL_LA_BARRISTER)
        .email(REPRESENTATIVE_EMAIL)
        .organisation("organisation")
        .telephoneNumber("07500045455")
        .build();
    private static final Long CASE_ID = 12345L;
    private static final String USER_ID = RandomStringUtils.randomAlphanumeric(10);
    private static final OrganisationUser USER = OrganisationUser.builder().userIdentifier(USER_ID).build();

    @MockBean
    private OrganisationApi organisationApi;
    @MockBean
    private UserService userService;

    ManageLegalRepresentativeMidEventControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldReturnValidationErrorForNonExistingUser() {
        givenFplService();
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL)).willThrow(
            mock(FeignException.NotFound.class));
        given(userService.getCaseRoles(CASE_ID)).willReturn(Set.of(CaseRole.CHILDSOLICITORA));

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actual = postMidEvent(callbackRequest);

        assertThat(actual.getErrors()).containsOnly(
            "Email address for LA counsel/External solicitor is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register"
        );
    }

    @Test
    void shouldValidateAnExistingUser() {
        givenFplService();
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL))
            .willReturn(USER);
        given(userService.getCaseRoles(CASE_ID)).willReturn(Set.of(CaseRole.CHILDSOLICITORA));

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        AboutToStartOrSubmitCallbackResponse actual = postMidEvent(callbackRequest);

        assertThat(actual.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorsWhenLALegalRepresentativeEmailsAreInvalid() {
        final CaseData caseData = CaseData.builder()
            .legalRepresentatives(wrapElements(LegalRepresentative.builder()
                    .email("Test user <Test.User@HMCTS.NET>")
                    .build(), LegalRepresentative.builder()
                    .email("test@test.com")
                    .build(),
                LegalRepresentative.builder()
                    .email("Test user <Test.User@HMCTS.NET>")
                    .build())).build();

        givenFplService();
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "Test user <Test.User@HMCTS.NET>"))
            .willReturn(USER);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, "test@test.com"))
            .willReturn(USER);
        given(userService.getCaseRoles(CASE_ID)).willReturn(Set.of(CaseRole.CHILDSOLICITORA));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "LA Legal Representative 1: Enter an email address in the correct format, for example name@example.com",
            "LA Legal Representative 3: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnErrorsWhenLALegalRepresentativeEmailIsValid() {
        givenFplService();
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL))
            .willReturn(USER);
        given(userService.getCaseRoles(CASE_ID)).willReturn(Set.of(CaseRole.CHILDSOLICITORA));

        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

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
