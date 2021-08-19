package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.aac.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.aac.model.DecisionRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

    private static final String USER_TOKEN = "UserToken";
    private static final String SYSTEM_TOKEN = "SystemToken";
    private static final String SERVICE_TOKEN = "ServiceToken";

    @Mock
    private RequestData requestData;

    @Mock
    private AuthTokenGenerator tokenGenerator;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private AboutToStartOrSubmitCallbackResponse expectedResponse;

    @InjectMocks
    private CaseAssignmentService underTest;

    @Test
    void shouldCallCaseAssignmentAsLoggedUser() {
        when(requestData.authorisation()).thenReturn(USER_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(caseAssignmentApi.applyDecision(any(), any(), any())).thenReturn(expectedResponse);

        final CaseDetails caseDetails = caseDetails();

        final AboutToStartOrSubmitCallbackResponse actualResponse = underTest.applyDecision(caseDetails);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(caseAssignmentApi).applyDecision(USER_TOKEN, SERVICE_TOKEN, decisionRequest(caseDetails));
    }

    @Test
    void shouldCallCaseAssignmentAsSystemUser() {
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(caseAssignmentApi.applyDecision(any(), any(), any())).thenReturn(expectedResponse);

        final CaseDetails caseDetails = caseDetails();

        final AboutToStartOrSubmitCallbackResponse actualResponse = underTest.applyDecisionAsSystemUser(caseDetails);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(caseAssignmentApi).applyDecision(SYSTEM_TOKEN, SERVICE_TOKEN, decisionRequest(caseDetails));
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder().id(RandomUtils.nextLong()).build();
    }

    private DecisionRequest decisionRequest(CaseDetails caseDetails) {
        return DecisionRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }
}
