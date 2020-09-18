package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final LocalAuthorityService localAuthorityNameService;
    private static final Organisation EMPTY_ORGANISATION = Organisation.builder().build();

    public List<String> validateIfUserIsOnboarded() {
        List<String> errors = new ArrayList<>();
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);

        if (!featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(localAuthorityName)) {

            if (organisationService.findOrganisation().equals(EMPTY_ORGANISATION)) {
                errors.add("Register for an account.");
                errors.add("You cannot start an online application until you’re fully registered.");
                errors.add("Ask your local authority’s public law administrator for help with registration.");
            }
        }

        return errors;
    }
}
