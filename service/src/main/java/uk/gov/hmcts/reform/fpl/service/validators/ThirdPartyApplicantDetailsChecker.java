package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RepresentingDetails;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
public class ThirdPartyApplicantDetailsChecker extends LocalAuthorityDetailsChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        final List<LocalAuthority> localAuthorities = unwrapElements(caseData.getLocalAuthorities());

        if (isEmpty(localAuthorities)) {
            return List.of("Add applicant's details");
        }

        return validateThirdPartyApplicant(localAuthorities.get(0));
    }

    private List<String> validateThirdPartyApplicant(LocalAuthority localAuthority) {

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

    @Override
    public List<String> validateAddress(Address address) {
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

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }
}
