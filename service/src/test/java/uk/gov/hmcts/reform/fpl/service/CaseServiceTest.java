package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CaseServiceTest {

    private static final String AUTH_TOKEN = "Bearer user token";
    private static final String SERVICE_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseUserApi caseUserApi;

    @InjectMocks
    private CaseService caseService;

    @Test
    void shouldAddUserToCase() {
        String caseId = RandomStringUtils.randomAlphabetic(10);
        String userId = RandomStringUtils.randomAlphabetic(10);
        Set<CaseRole> caseRoles = Set.of(CaseRole.SOLICITOR);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        caseService.addUser(AUTH_TOKEN, caseId, userId, caseRoles);

        verify(caseUserApi).updateCaseRolesForUser(
            AUTH_TOKEN,
            SERVICE_TOKEN,
            caseId,
            userId,
            new CaseUser(userId, Set.of("[SOLICITOR]")));
    }
}
