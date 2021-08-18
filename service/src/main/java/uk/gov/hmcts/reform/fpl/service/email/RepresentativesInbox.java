package uk.gov.hmcts.reform.fpl.service.email;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

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

    public LinkedHashSet<String> getRespondentSolicitorEmails(CaseData caseData,
                                                              RepresentativeServingPreferences preference) {
        return caseData.getAllRespondents().stream()
            .filter(respondent -> shouldSend(preference, respondent))
            .map(this::extractEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<String> getChildrenSolicitorEmails(CaseData caseData,
                                                            RepresentativeServingPreferences preference) {
        return caseData.getAllChildren().stream()
            .filter(child -> shouldSend(preference, child))
            .map(this::extractEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("unchecked")
    public Set<?> getNonSelectedRespondentsRecipients(
        RepresentativeServingPreferences servingPreferences,
        CaseData caseData,
        List<Element<Respondent>> respondentsSelected,
        Function<Element<Representative>, ?> mapperFunction) {

        Set<UUID> allRepresentativeIds = unwrapElements(caseData.getAllRespondents())
            .stream()
            .flatMap(respondent -> respondent.getRepresentedBy()
                .stream().map(Element::getValue))
            .collect(Collectors.toSet());

        Set<UUID> selectedRepresentativeIds = respondentsSelected.stream()
            .flatMap(otherElement -> otherElement.getValue().getRepresentedBy().stream().map(Element::getValue))
            .collect(Collectors.toSet());

        LinkedHashSet<Recipient> notSelectedRepresentedRespondents = (LinkedHashSet<Recipient>)
            caseData.getRepresentativesElementsByServedPreference(servingPreferences)
                .stream()
                .filter(representativeElement -> allRepresentativeIds.contains(representativeElement.getId()))
                .filter(not(representativeElement ->
                    selectedRepresentativeIds.contains(representativeElement.getId())))
                .map(mapperFunction)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final Set<Recipient> nonSelectedRespondents = new LinkedHashSet<>(notSelectedRepresentedRespondents);
        if (servingPreferences == POST) {
            nonSelectedRespondents.addAll(getNotSelectedUnrepresentedRespondents(caseData, respondentsSelected));
        }
        return nonSelectedRespondents;
    }

    private Set<Recipient> getNotSelectedUnrepresentedRespondents(CaseData caseData,
                                                                  List<Element<Respondent>> selectedRespondents) {
        List<Element<Respondent>> unrepresentedRespondents = caseData.getAllRespondents().stream()
            .filter(respondent -> isEmpty(respondent.getValue().getRepresentedBy())
                && !YES.getValue().equals(respondent.getValue().getLegalRepresentation()))
            .collect(toList());

        List<UUID> selectedRespondentsIds = selectedRespondents.stream().map(Element::getId).collect(toList());
        return unrepresentedRespondents.stream()
            .filter(respondentElement -> !selectedRespondentsIds.contains(respondentElement.getId()))
            .map(resp -> resp.getValue().getParty())
            .collect(Collectors.toSet());
    }

    public Set<Recipient> getSelectedRecipientsWithNoRepresentation(List<Element<Respondent>> selectedRespondents) {
        return selectedRespondents.stream()
            .map(Element::getValue)
            .filter(respondent -> isEmpty(respondent.getRepresentedBy()) && respondent.hasAddress())
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

    private String extractEmail(Element<? extends WithSolicitor> element) {
        return Optional.ofNullable(element.getValue().getSolicitor())
            .map(RespondentSolicitor::getEmail)
            .orElse(null);
    }

    private static boolean hasRole(Representative rep, List<RepresentativeRole.Type> roles) {
        RepresentativeRole.Type representativeRole = rep.getRole().getType();
        return roles.contains(representativeRole);
    }
}
