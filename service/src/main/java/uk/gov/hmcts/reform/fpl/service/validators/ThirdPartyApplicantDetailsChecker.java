package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RepresentingDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class ThirdPartyApplicantDetailsChecker implements EventChecker {

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
            return List.of("Add solicitor's details");
        }

        final List<String> errors = new ArrayList<>();

        errors.addAll(validateRepresentingDetails(localAuthority.getRepresentingDetails()));

        if (isBlank(localAuthority.getName())) {
            errors.add("Enter solicitor's name");
        }

        if (isBlank(localAuthority.getPbaNumber())) {
            errors.add("Enter solicitor's pba number");
        }

        if (isBlank(localAuthority.getCustomerReference())) {
            errors.add("Enter solicitor's customer reference");
        }

        errors.addAll(validateAddress(localAuthority.getAddress()));
        errors.addAll(validateAdditionalContacts(unwrapElements(localAuthority.getColleagues())));

        return errors;
    }

    private List<String> validateRepresentingDetails(RepresentingDetails representingDetails) {
        final List<String> errors = new ArrayList<>();

        if (isNull(representingDetails)) {
            errors.add("Enter details of person you are representing");
        } else {
            if (isBlank(representingDetails.getFirstName())
                || isBlank(representingDetails.getLastName())) {
                errors.add("Enter details of person you are representing");
            }
        }

        return errors;
    }

    private List<String> validateAddress(Address address) {
        if (isEmpty(address)) {
            return List.of("Enter solicitor's address");
        }

        final List<String> errors = new ArrayList<>();

        if (isBlank(address.getPostcode())) {
            errors.add("Enter solicitor's postcode");
        }

        if (isBlank(address.getAddressLine1())) {
            errors.add("Enter valid solicitor's address");
        }

        return errors;
    }

    private List<String> validateAdditionalContacts(List<Colleague> colleagues) {
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
            if (isBlank(colleague.getFullName())) {
                errors.add("Enter main contact full name");
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

        if (isBlank(colleague.getFullName())) {
            errors.add(format("Enter full name for other contact %d", index));
        }

        if (isBlank(colleague.getEmail())) {
            errors.add(format("Enter email for other contact %d", index));
        }

        return errors;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }
}
