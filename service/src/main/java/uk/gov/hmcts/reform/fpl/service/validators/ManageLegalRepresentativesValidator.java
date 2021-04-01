package uk.gov.hmcts.reform.fpl.service.validators;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageLegalRepresentativesValidator {

    private static final String VALIDATION_SUFFIX = "for Legal representative";

    private final OrganisationService organisationService;

    public List<String> validate(List<Element<LegalRepresentative>> legalRepresentatives) {
        return Streams.mapWithIndex(
            Optional.ofNullable(legalRepresentatives).orElse(newArrayList()).stream(),
            (legalRepresentativeElement, idx) ->
                performBasicValidation(legalRepresentativeElement.getValue(), idx, legalRepresentatives.size())
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> performBasicValidation(LegalRepresentative legalRepresentative, long currentRepresentativeIdx,
                                                int sizeOfRepresentatives) {
        List<String> validationErrors = newArrayList();

        Optional<String> userId = organisationService.findUserByEmail(legalRepresentative.getEmail());
        if (userId.isEmpty()) {
            validationErrors.add(
                validationMessageForInvalidEmail(
                    currentRepresentativeIdx,
                    sizeOfRepresentatives
                ));
        }

        return validationErrors;

    }

    private String validationMessageForInvalidEmail(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return String.format(
            "Email address for Legal representative%s is not registered on the system. "
                + "They can register at "
                + "https://manage-org.platform.hmcts.net/register-org/register",
            addNumericIfMultipleElements(currentRepresentativeIdx, sizeOfRepresentatives));
    }

    private String addNumericIfMultipleElements(long currentRepresentativeIdx, int sizeOfRepresentatives) {
        return sizeOfRepresentatives > 1 ? String.format(" %d", currentRepresentativeIdx + 1) : "";
    }

}
