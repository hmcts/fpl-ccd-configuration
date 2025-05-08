package uk.gov.hmcts.reform.fpl.service.others;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OtherRecipientsInbox {

    public <R> Set<R> getAllRecipients(RepresentativeServingPreferences servingPreferences, CaseData caseData,
                                       Function<Element<Representative>, R> mapperFunction) {

        return getAllOtherRepresentativeElements(servingPreferences, caseData)
            .map(mapperFunction)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public <R> Set<R> getNonSelectedRecipients(RepresentativeServingPreferences servingPreferences,
                                               CaseData caseData, List<Element<Other>> othersSelected,
                                               Function<Element<Representative>, R> mapperFunction) {


        Set<UUID> selectedRepresentativeIds = othersSelected.stream()
            .flatMap(otherElement -> otherElement.getValue().getRepresentedBy().stream().map(Element::getValue))
            .collect(Collectors.toSet());

        return getAllOtherRepresentativeElements(servingPreferences, caseData)
            .filter(representativeElement -> !selectedRepresentativeIds.contains(representativeElement.getId()))
            .map(mapperFunction)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Recipient> getSelectedRecipientsWithNoRepresentation(List<Element<Other>> othersSelectedElements) {

        return othersSelectedElements.stream()
            .map(Element::getValue)
            .filter(other -> other.hasAddressAdded() && !other.isRepresented() && !other.isDeceasedOrNFA())
            .map(Other::toParty)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    private Stream<Element<Representative>> getAllOtherRepresentativeElements(
        RepresentativeServingPreferences servingPreferences,
        CaseData caseData
    ) {
        Set<UUID> allOthersRepresentativeIds = caseData.getAllOthers().stream()
            .flatMap(otherElement -> otherElement.getValue().getRepresentedBy().stream().map(Element::getValue))
            .collect(Collectors.toSet());

        return caseData.getRepresentativesElementsByServedPreference(servingPreferences)
            .stream()
            .filter(representativeElement -> allOthersRepresentativeIds.contains(representativeElement.getId()));
    }
}
