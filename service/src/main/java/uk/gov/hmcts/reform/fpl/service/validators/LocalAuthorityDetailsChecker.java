package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class LocalAuthorityDetailsChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        final List<LocalAuthority> localAuthorities = unwrapElements(caseData.getLocalAuthorities());

        if (isEmpty(localAuthorities)) {
            return List.of("Add applicant's details");
        }

        return validateLocalAuthority(localAuthorities.get(0));
    }

    private List<String> validateLocalAuthority(LocalAuthority localAuthority) {
        if (isNull(localAuthority)) {
            return List.of("Add applicant's details");
        }

        final List<String> errors = new ArrayList<>();

        if (isBlank(localAuthority.getName())) {
            errors.add("Enter local authority's name");
        }

        if (isBlank(localAuthority.getPbaNumber())) {
            errors.add("Enter local authority's pba number");
        }

        errors.addAll(validateAddress(localAuthority.getAddress()));
        errors.addAll(validateAdditionalContacts(unwrapElements(localAuthority.getColleagues())));

        return errors;
    }

    private List<String> validateAddress(Address address) {
        if (isEmpty(address)) {
            return List.of("Enter local authority's address");
        }

        final List<String> errors = new ArrayList<>();

        if (isBlank(address.getPostcode())) {
            errors.add("Enter local authority's postcode");
        }

        if (isBlank(address.getAddressLine1())) {
            errors.add("Enter valid local authority's address");
        }

        return errors;
    }

    private List<String> validateAdditionalContacts(List<Colleague> colleagues) {
        final List<String> errors = new ArrayList<>();

        if (colleagues.isEmpty()) {
            errors.add("Add a colleague");
        } else if (colleagues.size() == 1) {
            errors.addAll(validateAdditionalContact(colleagues.get(0)));
        } else {
            for (int i = 0; i < colleagues.size(); i++) {
                errors.addAll(validateAdditionalContact(colleagues.get(i), i + 1));
            }
        }

        return errors;
    }

    private List<String> validateAdditionalContact(Colleague colleague) {
        final List<String> errors = new ArrayList<>();

        if (isEmpty(colleague.getRole())) {
            errors.add("Select colleague case role");
        } else if (colleague.getRole().equals(OTHER) && isBlank(colleague.getTitle())) {
            errors.add("Enter colleague title");
        }

        if (isBlank(colleague.getFullName())) {
            errors.add("Enter colleague full name");
        }

        if (!colleague.isMainContact() && isBlank(colleague.getEmail())) {
            errors.add("Enter colleague email");
        }

        return errors;
    }

    private List<String> validateAdditionalContact(Colleague colleague, int index) {
        final List<String> errors = new ArrayList<>();

        if (isEmpty(colleague.getRole())) {
            errors.add(format("Select case role for colleague %d", index));
        } else if (colleague.getRole().equals(OTHER) && isBlank(colleague.getTitle())) {
            errors.add(format("Enter title for colleague %d", index));
        }

        if (isBlank(colleague.getFullName())) {
            errors.add(format("Enter full name for colleague %d", index));
        }

        if (!colleague.isMainContact() && isBlank(colleague.getEmail())) {
            errors.add(format("Enter email for colleague %d", index));
        }

        return errors;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isNotEmpty(unwrapElements(caseData.getLocalAuthorities()));
    }
}
