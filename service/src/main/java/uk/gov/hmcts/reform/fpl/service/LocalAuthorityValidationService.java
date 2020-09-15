package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {
    private final OrganisationService organisationService;

    public List<String> validateIfLaIsOnboarded(final String localAuthorityName, String currentUser) {
        List<String> errors = new ArrayList<>();

        Set<String> users = organisationService.findUserIdsInSameOrganisation(localAuthorityName);

        if(!users.contains(currentUser)) {
            errors.add("You can't create a case as you are not onboarded");
        }

        return errors;
    }
}
