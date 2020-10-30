package uk.gov.hmcts.reform.fpl.service.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLegalRepresentativesValidator {

    private final OrganisationService organisationService;

    public List<String> validate(List<Element<LegalRepresentative>> legalRepresentatives) {
        log.info("validate {}",legalRepresentatives);
        return Optional.ofNullable(legalRepresentatives)
            .orElse(Lists.emptyList()).stream().flatMap(
                legalRepresentativeElement -> performBasicValidation(legalRepresentativeElement.getValue()).stream()
        ).collect(Collectors.toList());
    }

    private List<String> performBasicValidation(LegalRepresentative legalRepresentative) {
        List<String> validationErrors = Lists.newArrayList();
        if (isEmpty(legalRepresentative.getFullName())) {
            validationErrors.add(format("Enter a full name"));
        }

        if (isNull(legalRepresentative.getRole())) {
            validationErrors.add(format("Select a role in the case"));
        }

        if (isEmpty(legalRepresentative.getOrganisation())) {
            validationErrors.add(format("Enter an organisation name"));
        }

        if (isEmpty(legalRepresentative.getEmail())) {
            validationErrors.add(format("Enter an email address"));
        } else {
            Optional<String> userId = organisationService.findUserByEmail(legalRepresentative.getEmail());
            if (userId.isEmpty()) {
                validationErrors.add(
                    format("%s must already have an account with the digital service", legalRepresentative.getEmail()));
            }
        }

        if (isEmpty(legalRepresentative.getTelephoneNumber())) {
            validationErrors.add(format("Enter a phone number"));
        }

        return validationErrors;

    }
}
