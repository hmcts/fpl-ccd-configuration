package uk.gov.hmcts.reform.fpl.service.email;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativesInbox {

    public Set<String> getEmailsByPreference(CaseData caseData, RepresentativeServingPreferences preference) {
        if (preference.equals(POST)) {
            throw new IllegalArgumentException("Preference should not be POST");
        }

        LinkedHashSet<String> emails = getRepresentativeEmails(caseData, preference);
        emails.addAll(getRespondentSolicitorEmails(caseData, preference));
        emails.addAll(getChildrenSolicitorEmails(caseData, preference));

        return emails;
    }

    public LinkedHashSet<String> getRepresentativeEmails(CaseData caseData,
                                                         RepresentativeServingPreferences preference) {
        return caseData.getRepresentativesByServedPreference(preference)
            .stream()
            .map(Representative::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public HashSet<String> getRepresentativeEmailsFilteredByRole(CaseData caseData,
                                                                 RepresentativeServingPreferences preference,
                                                                 List<RepresentativeRole.Type> roles) {
        return caseData.getRepresentativesByServedPreference(preference)
            .stream()
            .filter(representative -> hasRole(representative, roles))
            .map(Representative::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public HashSet<String> getRespondentSolicitorEmails(CaseData caseData,
                                                        RepresentativeServingPreferences preference) {
        return getRespondentSolicitorEmailsFromList(caseData.getAllRespondents(), preference);
    }

    public HashSet<String> getRespondentSolicitorEmailsFromList(List<Element<Respondent>> respondents,
                                                        RepresentativeServingPreferences preference) {
        return respondents.stream()
            .filter(respondent -> shouldSend(preference, respondent))
            .map(this::extractEmailsForSolicitorAndColleagues)
            .flatMap(Collection::stream).collect(Collectors.toList())
            .stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public HashSet<String> getChildrenSolicitorEmails(CaseData caseData,
                                                      RepresentativeServingPreferences preference) {
        return getChildrenSolicitorEmailsFromList(caseData.getAllChildren(), preference);
    }

    public HashSet<String> getChildrenSolicitorEmailsFromList(List<Element<Child>> children,
                                                      RepresentativeServingPreferences preference) {
        return children.stream()
            .filter(child -> shouldSend(preference, child))
            .map(this::extractEmailsForSolicitorAndColleagues)
            .flatMap(Collection::stream).collect(Collectors.toList())
            .stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Recipient> getRecipientsWithNoRepresentation(List<Element<Respondent>> selectedRespondents) {
        return selectedRespondents.stream()
            .map(Element::getValue)
            .filter(respondent -> isEmpty(respondent.getRepresentedBy())
                && isEmpty(respondent.getSolicitor())
                && respondent.hasAddress()
                && !respondent.isDeceasedOrNFA())
            .map(Respondent::toParty)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean shouldSend(RepresentativeServingPreferences preference,
                               Element<? extends WithSolicitor> element) {
        if (RepresentativeServingPreferences.DIGITAL_SERVICE == preference) {
            return element.getValue().hasRegisteredOrganisation();
        }

        return !element.getValue().hasRegisteredOrganisation();
    }

    private List<String> extractEmailsForSolicitorAndColleagues(Element<? extends WithSolicitor> element) {
        List<String> colleagues = Optional.ofNullable(element.getValue().getSolicitor())
            .map(RespondentSolicitor::getColleaguesToBeNotified)
            .map(elements -> elements.stream()
                .map(Element::getValue)
                .map(Colleague::getEmail)
                .collect(Collectors.toList())
            ).orElse(new ArrayList<>());
        Optional.ofNullable(element.getValue().getSolicitor()).ifPresent(
            el -> colleagues.add(el.getEmail())
        );

        return colleagues;
    }

    private static boolean hasRole(Representative rep, List<RepresentativeRole.Type> roles) {
        RepresentativeRole.Type representativeRole = rep.getRole().getType();
        return roles.contains(representativeRole);
    }
}
