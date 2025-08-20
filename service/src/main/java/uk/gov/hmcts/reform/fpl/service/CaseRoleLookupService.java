package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CaseRoleLookupService {

    private final CaseAccessService caseAccessService;

    public List<SolicitorRole> getCaseSolicitorRolesForCurrentUser(Long caseId) {
        return getCaseSolicitorRolesByCaseRoles(caseAccessService.getUserCaseRoles(caseId));
    }

    public List<SolicitorRole> getCaseSolicitorRolesByCaseRoles(Set<CaseRole> caseRoles) {
        return caseRoles.stream()
            .map(CaseRole::formattedName)
            .map(SolicitorRole::from)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
