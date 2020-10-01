package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final LocalAuthorityService localAuthorityNameService;

    public List<String> validateIfUserIsOnboarded() {
        List<String> errors = new ArrayList<>();
        String caseLocalAuthority = localAuthorityNameService.getLocalAuthorityCode();
        String localAuthorityName = localAuthorityNameLookupConfiguration.getLocalAuthorityName(caseLocalAuthority);

        if (!featureToggleService.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(localAuthorityName)
            && organisationService.findOrganisation().isEmpty()) {
            errors.add("Register for an account.");
            errors.add("You cannot start an online application until you’re fully registered.");
            errors.add("Ask your local authority’s public law administrator, or email MyHMCTSsupport@justice.gov.uk, "
                + "for help with registration.");
        }

        return errors;
    }
}
