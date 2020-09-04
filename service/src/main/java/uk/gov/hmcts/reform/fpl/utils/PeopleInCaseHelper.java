package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class PeopleInCaseHelper {

    private PeopleInCaseHelper() {
        // NO-OP
    }

    public static String getFirstApplicantName(List<Element<Applicant>> applicants) {
        return ElementUtils.unwrapElements(applicants).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(Applicant::getParty)
            .map(ApplicantParty::getOrganisationName)
            .orElse("");
    }

    public static String getFirstRespondentLastName(List<Element<Respondent>> respondents) {
        return getFirstRespondentParty(respondents)
            .map(RespondentParty::getLastName)
            .orElse("");
    }

    public static String getFirstRespondentLastName(CaseData caseData) {
        return getFirstRespondentLastName(caseData.getAllRespondents());
    }

    public static String getFirstRespondentFullName(List<Element<Respondent>> respondents) {
        return getFirstRespondentParty(respondents)
            .map(RespondentParty::getFullName)
            .orElse("");
    }

    //To be deleted when print and post is available to use as part of FPLA-1287
    public static List<String> formatRepresentativesForPostNotification(List<Representative> representatives) {
        return representatives.stream()
            .map(representative -> String.format("%s%n%s", representative.getFullName(),
                representative.getAddress().getAddressAsString(", ")))
            .collect(toList());
    }

    private static Optional<RespondentParty> getFirstRespondentParty(List<Element<Respondent>> respondents) {
        return ElementUtils.unwrapElements(respondents).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(Respondent::getParty);
    }
}
