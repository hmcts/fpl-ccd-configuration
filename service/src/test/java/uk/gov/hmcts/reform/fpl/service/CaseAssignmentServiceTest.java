package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.aac.client.NocApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.reform.ccd.model.Organisation.organisation;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

    private static final String USER_TOKEN = "UserToken";
    private static final String SYSTEM_TOKEN = "SystemToken";
    private static final String SERVICE_TOKEN = "ServiceToken";

    @Spy
    private Time time = new FixedTimeConfiguration().stoppedTime();

    @Mock
    private RequestData requestData;

    @Mock
    private AuthTokenGenerator tokenGenerator;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private NocApi nocApi;

    @Mock
    private AboutToStartOrSubmitCallbackResponse expectedResponse;

    @InjectMocks
    private CaseAssignmentService underTest;

    @Test
    void shouldCallCaseAssignmentAsLoggedUser() {
        when(requestData.authorisation()).thenReturn(USER_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(nocApi.applyDecision(any(), any(), any())).thenReturn(expectedResponse);

        final CaseDetails caseDetails = caseDetails();

        final AboutToStartOrSubmitCallbackResponse actualResponse = underTest.applyDecision(caseDetails);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(nocApi).applyDecision(USER_TOKEN, SERVICE_TOKEN, decisionRequest(caseDetails));
    }

    @Test
    void shouldCallCaseAssignmentAsSystemUser() {
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(nocApi.applyDecision(any(), any(), any())).thenReturn(expectedResponse);

        final CaseDetails caseDetails = caseDetails();

        final AboutToStartOrSubmitCallbackResponse actualResponse = underTest.applyDecisionAsSystemUser(caseDetails);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(nocApi).applyDecision(SYSTEM_TOKEN, SERVICE_TOKEN, decisionRequest(caseDetails));
    }

    @Test
    void shouldBuildReplacementRequestsAndCallCaseAssignmentAsSystemUser() {
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(nocApi.applyDecision(any(), any(), any())).thenReturn(expectedResponse);

        final CaseDetails caseDetails = caseDetails();
        final Organisation organisationToAdd = organisation("ORG1");
        final Organisation organisationToRemove = organisation("ORG2");

        final AboutToStartOrSubmitCallbackResponse actualResponse = underTest.replaceAsSystemUser(caseDetails,
            LASHARED, organisationToAdd, organisationToRemove);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(nocApi).applyDecision(SYSTEM_TOKEN, SERVICE_TOKEN, decisionRequest(caseDetails));

        final DynamicListElement expectedRoleItem = DynamicListElement.builder()
            .code("[LASHARED]")
            .label("[LASHARED]")
            .build();

        final ChangeOrganisationRequest expectedChangeRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(time.now())
            .caseRoleId(DynamicList.builder()
                .value(expectedRoleItem)
                .listItems(List.of(expectedRoleItem))
                .build())
            .organisationToRemove(organisationToRemove)
            .organisationToAdd(organisationToAdd)
            .build();

        assertThat(caseDetails.getData().get("changeOrganisationRequestField")).isEqualTo(expectedChangeRequest);
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder().id(RandomUtils.nextLong()).data(new HashMap<>()).build();
    }

    private DecisionRequest decisionRequest(CaseDetails caseDetails) {
        return DecisionRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
