package uk.gov.hmcts.reform.fpl.service.validators;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLegalRepresentativesValidator {

    private static final String VALIDATION_SUFFIX = "for Legal representative";

    private final OrganisationService organisationService;

    public List<String> validate(List<Element<LegalRepresentative>> legalRepresentatives) {
        return Streams.mapWithIndex(
            Optional.ofNullable(legalRepresentatives).orElse(Lists.emptyList()).stream(),
            (legalRepresentativeElement, idx) ->
                performBasicValidation(legalRepresentativeElement.getValue(), idx, legalRepresentatives.size())
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> performBasicValidation(LegalRepresentative legalRepresentative, long currentRepresentativeIdx,
                                                int sizeOfRepresentatives) {
        List<String> validationErrors = Lists.newArrayList();
        if (isEmpty(legalRepresentative.getFullName())) {
            validationErrors.add(validationMessage("Enter a full name",
                currentRepresentativeIdx, sizeOfRepresentatives));
        }

        if (isNull(legalRepresentative.getRole())) {
            validationErrors.add(validationMessage("Select a role",
                currentRepresentativeIdx, sizeOfRepresentatives));
        }

        if (isEmpty(legalRepresentative.getOrganisation())) {
            validationErrors.add(validationMessage("Enter an organisation",
                currentRepresentativeIdx,
                sizeOfRepresentatives));
        }

        if (isEmpty(legalRepresentative.getEmail())) {
            validationErrors.add(validationMessage("Enter an email address",
                currentRepresentativeIdx,
                sizeOfRepresentatives));
        } else {
            Optional<String> userId = organisationService.findUserByEmail(legalRepresentative.getEmail());
            if (userId.isEmpty()) {
                validationErrors.add(
                    validationMessageForInvalidEmail(currentRepresentativeIdx, sizeOfRepresentatives));
            }
        }

        if (isEmpty(legalRepresentative.getTelephoneNumber())) {
            validationErrors.add(validationMessage("Enter a phone number",
                currentRepresentativeIdx,
                sizeOfRepresentatives)
            );
        }

        return validationErrors;

    }

    private String validationMessage(String prefix, long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return MessageFormat.format("{0} {1}{2}",
            prefix,
            VALIDATION_SUFFIX,
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String validationMessageForInvalidEmail(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return String.format(
            "Email address for Legal representative%s is not registered on the system.<br/>They can "
                + "register at <a href='https://manage-org.platform.hmcts"
                + ".net/register-org/register'>"
                + "https://manage-org.platform.hmcts"
                + ".net/register-org/register</a>",
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String addNumericIfMultipleElements(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return sizeOfRepresentatives > 1 ? String.format(" %d", currentRepresentativeIdx + 1) : "";
    }

}
