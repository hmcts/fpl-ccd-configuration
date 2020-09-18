package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityValidationService {
    private final OrganisationService organisationService;

    public List<String> validateIfUserIsOnboarded() {
        List<String> errors = new ArrayList<>();

        if(organisationService.findOrganisation().equals(Organisation.builder().build())) {
                errors.add("Register for an account.");
                errors.add("You cannot start an online application until you’re fully registered.");
                errors.add("Ask your local authority’s public law administrator for help with registration.");
        }

        return errors;
    }
}
