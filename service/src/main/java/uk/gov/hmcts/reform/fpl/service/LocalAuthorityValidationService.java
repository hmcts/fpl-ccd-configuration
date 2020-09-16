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

    public List<String> validateIfLaIsOnboarded(final String localAuthorityCode, String currentUser) {
        List<String> errors = new ArrayList<>();

        Set<String> users = organisationService.findUserIdsInSameOrganisation(localAuthorityCode);

        if(!users.contains(currentUser)) {
            errors.add("Register for an account");
            errors.add("You cannot start an online application until you’re fully registered.");
            errors.add("Press the back button on your browser to access the link.");
        }

        return errors;
    }
}
