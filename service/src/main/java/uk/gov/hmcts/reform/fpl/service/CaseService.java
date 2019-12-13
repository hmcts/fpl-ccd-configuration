package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CaseService {

    private final CaseUserApi caseUserApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public CaseService(CaseUserApi caseUserApi, AuthTokenGenerator authTokenGenerator) {
        this.caseUserApi = caseUserApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public void addUser(String authorisation, String caseId, String userId, Set<CaseRole> caseRoles) {
        Set<String> formattedCaseRoles = caseRoles.stream().map(CaseRole::formattedName).collect(Collectors.toSet());
        CaseUser caseUser = new CaseUser(userId, formattedCaseRoles);
        caseUserApi.updateCaseRolesForUser(authorisation, authTokenGenerator.generate(), caseId, userId, caseUser);
    }
}
