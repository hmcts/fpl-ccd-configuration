package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {
    private final OrganisationService organisationService;
    private final RequestData requestData;

    public List<String> validateIfLaIsOnboarded(final String localAuthorityCode) {
        List<String> errors = new ArrayList<>();
        String currentUser = requestData.userId();

        Set<String> users = organisationService.findUserIdsInSameOrganisation(localAuthorityCode);

        if (!users.contains(currentUser)) {
            errors.add("Register for an account.");
            errors.add("You cannot start an online application until you’re fully registered.");
            errors.add("Ask your local authority’s public law administrator for help with registration.");
        }

        return errors;
    }
}
