package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.LegalRepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LABARRISTER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(RepresentativesController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalRepresentativeAboutToSubmitControllerTest extends AbstractControllerTest {

    public static final String SERVICE_AUTH_TOKEN = RandomStringUtils.randomAlphanumeric(10);
    public static final String REPRESENTATIVE_EMAIL = "test@test.com";
    public static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName("John Smith")
        .role(LegalRepresentativeRole.EXTERNAL_LA_BARRISTER)
        .email(REPRESENTATIVE_EMAIL)
        .organisation("organisation")
        .telephoneNumber("07500045455")
        .build();
    private static final Long CASE_ID = 12345L;
    public static final String USER_ID = RandomStringUtils.randomAlphanumeric(10);

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseUserApi caseUserApi;


    ManageLegalRepresentativeAboutToSubmitControllerTest() {
        super("manage-legal-representatives");
    }

    @Test
    void shouldAddLegalRepresentativeToCase() {

        CaseDetails caseDetailsBefore = buildCaseData(emptyList());
        CaseDetails caseDetails = buildCaseData(List.of(element(LEGAL_REPRESENTATIVE)));

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(organisationApi.findUserByEmail(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, REPRESENTATIVE_EMAIL))
            .willReturn(new OrganisationUser(USER_ID));

        CallbackRequest callbackRequest = buildCallbackRequest(caseDetailsBefore, caseDetails);

        postAboutToSubmitEvent(callbackRequest);

        verify(caseUserApi).updateCaseRolesForUser(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,
            caseDetails.getId().toString(), USER_ID, new CaseUser(USER_ID, Set.of(LABARRISTER.formattedName())));
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
