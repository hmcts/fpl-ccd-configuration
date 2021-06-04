package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.service.notify.NotificationClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.MANAGING_ORGANISATION_REMOVED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.ASYNC_MAX_TIMEOUT;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.caseRoleDynamicList;

@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class ManagingOrganisationRemovalControllerTest extends AbstractCallbackTest {

    private static final String ORGANISATION_ID = "ORG1";

    @Autowired
    private Time time;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private NotificationClient notificationClient;

    @Captor
    private ArgumentCaptor<DecisionRequest> removalRequestCaptor;

    ManagingOrganisationRemovalControllerTest() {
        super("remove-managing-organisation");
    }

    @BeforeEach
    void setup() {
        givenFplService();
        givenSystemUser();
    }

    @Test
    void shouldAddNameOfManagingOrganisation() {
        final Organisation organisation = Organisation.builder()
            .organisationIdentifier(ORGANISATION_ID)
            .name("London Solicitors")
            .build();

        given(organisationApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
            .willReturn(organisation);

        CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(organisation(ORGANISATION_ID))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        assertThat(response.getData()).containsEntry("managingOrganisationName", "London Solicitors");
    }

    @Test
    void shouldRemoveManagingOrganisation() {
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(organisation(ORGANISATION_ID))
                .orgPolicyCaseAssignedRole(EPSMANAGING.formattedName())
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse
            .builder()
            .build();

        given(caseAssignmentApi.applyDecision(eq(USER_AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN),
            removalRequestCaptor.capture())).willReturn(expectedResponse);

        final AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(caseData);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        final CaseData updatedCaseData = extractCaseData(removalRequestCaptor.getValue().getCaseDetails());

        final ChangeOrganisationRequest actualChangeRequest = updatedCaseData.getChangeOrganisationRequestField();

        final ChangeOrganisationRequest expectedChangeRequest = ChangeOrganisationRequest.builder()
            .caseRoleId(caseRoleDynamicList(EPSMANAGING.formattedName()))
            .requestTimestamp(time.now())
            .approvalStatus(APPROVED)
            .organisationToRemove(caseData.getOutsourcingPolicy().getOrganisation())
            .build();

        assertThat(actualChangeRequest).isEqualTo(expectedChangeRequest);
    }

    @Test
    void shouldSendEmailToRemovedManagingOrganisation() throws Exception {
        String managingOrganisationSolicitorEmail = "john@london.solicitors.com";

        final Organisation organisation = Organisation.builder()
            .organisationIdentifier(ORGANISATION_ID)
            .name("London Solicitors")
            .build();

        final OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation(ORGANISATION_ID))
            .orgPolicyCaseAssignedRole(EPSMANAGING.formattedName())
            .build();

        final Solicitor managingOrganisationSolicitor = Solicitor.builder()
            .email(managingOrganisationSolicitorEmail)
            .build();

        given(organisationApi.findOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
            .willReturn(organisation);

        final CaseData caseDataBefore = CaseData.builder()
            .id(10L)
            .caseName("Smith case")
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .outsourcingPolicy(organisationPolicy)
            .solicitor(managingOrganisationSolicitor)
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .outsourcingPolicy(organisationPolicy.toBuilder()
                .organisation(organisation(null))
                .build())
            .build();

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verify(notificationClient, timeout(ASYNC_MAX_TIMEOUT)).sendEmail(
            MANAGING_ORGANISATION_REMOVED_TEMPLATE,
            managingOrganisationSolicitorEmail,
            Map.of(
                "caseNumber", caseData.getId(),
                "caseName", caseData.getCaseName(),
                "localAuthorityName", caseData.getCaseLocalAuthorityName(),
                "managingOrganisationName", organisation.getName()
            ),
            "localhost/" + caseData.getId()
        );
    }
}
