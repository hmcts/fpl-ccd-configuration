package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.Constants.USER_ID;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class CaseRoleLookupServiceTest {

    @Mock
    private CaseAccessDataStoreApi api;

    @Mock
    private RequestData requestData;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CaseRoleLookupService underTest;

    @Test
    void shouldGetCaseSolicitorRoles() {
        when(requestData.authorisation()).thenReturn(USER_AUTH_TOKEN);
        when(requestData.userId()).thenReturn(USER_ID);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(api.getUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, asList(TEST_CASE_ID), asList(USER_ID)))
            .thenReturn(
                CaseAssignedUserRolesResource.builder().caseAssignedUserRoles(asList(
                    CaseAssignedUserRole.builder().caseRole("[NOT_RELATED_TO_THIS]").build(),
                    CaseAssignedUserRole.builder().caseRole("[SOLICITORA]").build(),
                    CaseAssignedUserRole.builder().caseRole("[CHILDSOLICITORB]").build()
                )).build()
            );

        List<SolicitorRole> caseSolicitorRoles = underTest.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID);

        assertThat(caseSolicitorRoles)
            .hasSize(2)
            .contains(SOLICITORA, CHILDSOLICITORB);
    }

}
