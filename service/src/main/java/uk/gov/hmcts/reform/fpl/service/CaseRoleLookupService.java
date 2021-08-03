package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CaseRoleLookupService {

    private final CaseAccessDataStoreApi api;
    private final RequestData requestData;
    private final AuthTokenGenerator authTokenGenerator;

    public List<SolicitorRole> getCaseSolicitorRolesForCurrentUser(String caseId) {
        CaseAssignedUserRolesResource userRoles = api.getUserRoles(requestData.authorisation(),
            authTokenGenerator.generate(),
            List.of(caseId),
            List.of(requestData.userId()));

        return userRoles.getCaseAssignedUserRoles().stream()
            .map(CaseAssignedUserRole::getCaseRole)
            .map(SolicitorRole::from)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

}
