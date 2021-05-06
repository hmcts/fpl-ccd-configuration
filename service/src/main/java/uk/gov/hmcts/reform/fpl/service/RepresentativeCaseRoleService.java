package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.Representative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@Service
public class RepresentativeCaseRoleService {

    public Map<String, Set<CaseRole>> calculateCaseRoleUpdates(List<Representative> newRepresentativeList,
                                                               List<Representative> oldRepresentativeList) {

        Map<String, Set<CaseRole>> caseRolesByRepresentativeEmail = new HashMap<>();

        Map<String, Set<CaseRole>> newCaseRolesByRepresentativeEmail = caseRolesByEmail(newRepresentativeList);
        Map<String, Set<CaseRole>> oldCaseRolesByRepresentativeEmail = caseRolesByEmail(oldRepresentativeList);

        newCaseRolesByRepresentativeEmail.forEach((email, caseRoles) -> {
            if (!oldCaseRolesByRepresentativeEmail.getOrDefault(email, emptySet()).equals(caseRoles)) {
                caseRolesByRepresentativeEmail.put(email, caseRoles);
            }
        });

        oldCaseRolesByRepresentativeEmail.keySet().stream()
            .filter(email -> !newCaseRolesByRepresentativeEmail.containsKey(email))
            .forEach(email -> caseRolesByRepresentativeEmail.put(email, emptySet()));

        return caseRolesByRepresentativeEmail;
    }

    private static Map<String, Set<CaseRole>> caseRolesByEmail(List<Representative> representatives) {
        return representatives.stream()
            .filter(representative -> representative.getServingPreferences().equals(DIGITAL_SERVICE))
            .collect(groupingBy(
                Representative::getEmail,
                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                flatMapping(representative -> representative.getRole().getCaseRoles().stream(), toSet())
            ));
    }

}
