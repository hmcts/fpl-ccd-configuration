package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.ConfidentialParty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

public class PeopleInCaseHelper {

    private PeopleInCaseHelper() {
        // NO-OP
    }

    public static String getFirstApplicantName(CaseData caseData) {
        if (isNotEmpty(caseData.getLocalAuthorities())) {
            return Optional.ofNullable(caseData.getDesignatedLocalAuthority())
                .map(LocalAuthority::getName)
                .orElse("");
        }

        return unwrapElements(caseData.getApplicants()).stream()
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
        return unwrapElements(respondents).stream()
            .filter(Objects::nonNull)
            .findFirst()
            .map(Respondent::getParty);
    }

    private static Map<UUID, Address> addressByConfidentialPartyId(
        List<Element<? extends ConfidentialParty<?>>> parties) {
        return parties.stream().collect(
            HashMap::new,
            (container, party) -> container.put(party.getId(), party.getValue().toParty().getAddress()),
            HashMap::putAll
        );
    }

    public static boolean hasAddressChange(List<Element<? extends ConfidentialParty<?>>> after,
                                           List<Element<? extends ConfidentialParty<?>>> before) {
        Map<UUID, Address> beforeChildAddresses = addressByConfidentialPartyId(before);
        for (Element<? extends ConfidentialParty> a : after) {
            Address beforeAddress = beforeChildAddresses.get(a.getId());
            if (beforeAddress != null) {
                if (a.getValue().toParty().getAddress() == null
                    || !a.getValue().toParty().getAddress().equals(beforeAddress)) {
                    return true;
                }
            }
        }
        return false;
    }
}
