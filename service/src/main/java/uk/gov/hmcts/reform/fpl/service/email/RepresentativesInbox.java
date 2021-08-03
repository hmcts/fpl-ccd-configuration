package uk.gov.hmcts.reform.fpl.service.email;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativesInbox {

    public Set<String> getEmailsByPreference(CaseData caseData, RepresentativeServingPreferences preference) {
        if (preference.equals(RepresentativeServingPreferences.POST)) {
            throw new IllegalArgumentException("Preference should not be POST");
        }

        LinkedHashSet<String> emails = caseData.getRepresentativesByServedPreference(preference)
            .stream()
            .map(Representative::getEmail)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        emails.addAll(
            caseData.getAllRespondents().stream()
                .filter(respondent -> shouldSend(preference, respondent))
                .map(this::extractEmail)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new))
        );

        emails.addAll(
            caseData.getAllChildren().stream()
                .filter(child -> shouldSend(preference, child))
                .map(this::extractEmail)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new))
        );

        return emails;
    }

    public Set<?> getNonSelectedRespondentsRecipients(
        RepresentativeServingPreferences servingPreferences,
        CaseData caseData,
        List<Element<Respondent>> respondentsSelected,
        Function<Element<Representative>, ?> mapperFunction) {

        Set<UUID> representativeIds = caseData.getAllRespondents().stream()
            .flatMap(respondentElement -> respondentElement.getValue().getRepresentedBy()
                .stream().map(Element::getValue))
            .collect(Collectors.toSet());

        Set<UUID> selectedRepresentativeIds = respondentsSelected.stream()
            .flatMap(otherElement -> otherElement.getValue().getRepresentedBy().stream().map(Element::getValue))
            .collect(Collectors.toSet());

        return caseData.getRepresentativesElementsByServedPreference(servingPreferences)
            .stream()
            .filter(representativeElement -> representativeIds.contains(representativeElement.getId()))
            .filter(representativeElement -> !selectedRepresentativeIds.contains(representativeElement.getId()))
            .map(mapperFunction)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Recipient> getSelectedRecipientsWithNoRepresentation(List<Element<Respondent>> selectedRespondents) {
        return selectedRespondents.stream()
            .map(Element::getValue)
            .filter(respondent -> isEmpty(respondent.getRepresentedBy())
                && !isNull(respondent.getParty().getAddress())
                && isNotEmpty(respondent.getParty().getAddress().getPostcode()))
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
}
