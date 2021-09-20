package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.allEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyAddress;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyEmail;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.isEmptyTelephone;

@Component
@Deprecated
public class OrganisationDetailsChecker extends PropertiesChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("applicants", "solicitor"));
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final List<Applicant> applicants = ElementUtils.unwrapElements(caseData.getAllApplicants());
        final Solicitor solicitor = caseData.getSolicitor();

        if (!isEmptySolicitor(solicitor)) {
            return true;
        }

        switch (applicants.size()) {
            case 0:
                return false;
            case 1:
                return !isEmptyApplicant(applicants.get(0));
            default:
                return true;
        }
    }


    private static boolean isEmptyApplicant(Applicant applicant) {

        if (isEmpty(applicant)) {
            return true;
        }

        final ApplicantParty applicantParty = applicant.getParty();

        if (isEmpty(applicantParty)) {
            return true;
        }

        return isEmptyAddress(applicantParty.getAddress())
                && isEmptyTelephone(applicantParty.getMobileNumber())
                && isEmptyTelephone(applicantParty.getTelephoneNumber())
                && isEmptyEmail(applicantParty.getEmail())
                && allEmpty(
                applicantParty.getOrganisationName(),
                applicantParty.getPbaNumber(),
                applicantParty.getClientCode(),
                applicantParty.getCustomerReference(),
                applicantParty.getJobTitle());
    }

    private static boolean isEmptySolicitor(Solicitor solicitor) {
        if (isEmpty(solicitor)) {
            return true;
        }
        return allEmpty(
                solicitor.getName(),
                solicitor.getMobile(),
                solicitor.getTelephone(),
                solicitor.getEmail(),
                solicitor.getDx(),
                solicitor.getReference());
    }

}
