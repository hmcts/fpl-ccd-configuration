package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
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
            errors.add("Enter applicant's name");
        }

        if (isBlank(localAuthority.getPbaNumber())) {
            errors.add("Enter applicant's pba number");
        }

        if (isBlank(localAuthority.getCustomerReference())) {
            errors.add("Enter applicant's customer reference");
        }

        errors.addAll(validateAddress(localAuthority.getAddress()));
        errors.addAll(validateAdditionalContacts(unwrapElements(localAuthority.getColleagues())));

        return errors;
    }

    public List<String> validateAddress(Address address) {
        if (isEmpty(address)) {
            return List.of("Enter applicant's address");
        }

        final List<String> errors = new ArrayList<>();

        if (isBlank(address.getPostcode())) {
            errors.add("Enter applicant's postcode");
        }

        if (isBlank(address.getAddressLine1())) {
            errors.add("Enter valid applicant's address");
        }

        return errors;
    }

    protected List<String> validateAdditionalContacts(List<Colleague> colleagues) {
        final List<String> errors = new ArrayList<>();

        final Optional<Colleague> mainContact = colleagues.stream()
            .filter(Colleague::checkIfMainContact)
            .findFirst();

        if (mainContact.isPresent()) {
            errors.addAll(validateMainContact(mainContact.get()));
        } else {
            errors.add("Enter main contact");
        }

        final List<Colleague> otherContacts = colleagues.stream()
            .filter(colleague -> !colleague.checkIfMainContact())
            .toList();

        for (int i = 0; i < otherContacts.size(); i++) {
            errors.addAll(validateOtherContact(otherContacts.get(i), i + 1));
        }

        return errors;
    }

    private List<String> validateMainContact(Colleague colleague) {
        final List<String> errors = new ArrayList<>();

        if (colleague.checkIfMainContact()) {
            if (isBlank(colleague.getFirstName())) {
                errors.add("Enter main contact first name");
            }

            if (isBlank(colleague.getLastName())) {
                errors.add("Enter main contact last name");
            }

            if (isBlank(colleague.getPhone())) {
                errors.add("Enter main contact phone number");
            }
        } else {
            errors.add("Enter main contact");
        }

        return errors;
    }

    private List<String> validateOtherContact(Colleague colleague, int index) {
        final List<String> errors = new ArrayList<>();

        if (isEmpty(colleague.getRole())) {
            errors.add(format("Select case role for other contact %d", index));
        } else if (colleague.getRole().equals(OTHER) && isBlank(colleague.getTitle())) {
            errors.add(format("Enter title for other contact %d", index));
        }

        if (isBlank(colleague.getFirstName())) {
            errors.add(format("Enter first name for other contact %d", index));
        }

        if (isBlank(colleague.getLastName())) {
            errors.add(format("Enter last name for other contact %d", index));
        }

        if (isBlank(colleague.getEmail())) {
            errors.add(format("Enter email for other contact %d", index));
        }

        return errors;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return isNotEmpty(unwrapElements(caseData.getLocalAuthorities()));
    }
}
