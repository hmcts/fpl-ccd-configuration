package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORA;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class CaseRoleLookupServiceTest {

    @Mock
    private CaseAccessService caseAccessService;

    @InjectMocks
    private CaseRoleLookupService underTest;

    @Test
    void shouldGetCaseSolicitorRoles() {
        when(caseAccessService.getUserCaseRoles(TEST_CASE_ID))
            .thenReturn(Set.of(CaseRole.LABARRISTER, CaseRole.SOLICITORA, CaseRole.CHILDSOLICITORB));

        List<SolicitorRole> caseSolicitorRoles = underTest.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID);

        assertThat(caseSolicitorRoles)
            .hasSize(2)
            .contains(SOLICITORA, CHILDSOLICITORB);
    }

}
