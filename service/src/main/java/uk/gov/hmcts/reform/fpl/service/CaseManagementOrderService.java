package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Directions;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.model.Directions.getMapping;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final CaseManagementOrderGenerationService templateDataGenerationService;
    private final DocumentService documentService;

    public Document getOrderDocument(CaseData caseData) {
        DocmosisCaseManagementOrder templateData = templateDataGenerationService.getTemplateData(caseData);
        return documentService.getDocumentFromDocmosisOrderTemplate(templateData, CMO);
    }

    public void removeTransientObjectsFromCaseData(Map<String, Object> caseData) {
        final Set<String> keysToRemove = Set.of(HEARING_DATE_LIST.getKey(), SCHEDULE.getKey(), RECITALS.getKey());

        keysToRemove.forEach(caseData::remove);
    }

    public void prepareCustomDirections(CaseDetails caseDetails, CaseManagementOrder caseManagementOrder) {
        ofNullable(caseManagementOrder).ifPresentOrElse(
            order -> addDirections(caseDetails, order.getDirections()), () -> removeDirections(caseDetails));
    }

    public DynamicList getHearingDateDynamicList(CaseData caseData, CaseManagementOrder order) {
        List<DynamicListElement> values = getDateElements(caseData, false);

        DynamicListElement selectedValue = ofNullable(order)
            .map(x -> getPreselectedDate(values, x.getId()))
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder()
            .listItems(values)
            .value(selectedValue)
            .build();
    }

    public DynamicList getNextHearingDateDynamicList(CaseData caseData) {
        return DynamicList.builder()
            .listItems(getDateElements(caseData, true))
            .value(DynamicListElement.EMPTY)
            .build();
    }

    private void addDirections(CaseDetails caseDetails, List<Element<Direction>> directions) {
        getMapping(directions)
            .forEach((key, value) -> caseDetails.getData().put(key.toCaseManagementOrderDirectionField(), value));
    }

    private void removeDirections(CaseDetails caseDetails) {
        Stream.of(Directions.class.getDeclaredFields()).forEach(field -> caseDetails.getData().remove(field.getName()));
    }

    private List<DynamicListElement> getDateElements(CaseData caseData, boolean excludePastDates) {
        var sealedCmoHearingDateIds = getSealedCmoHearingDateIds(caseData);

        var hearingDetailsStream = caseData.getHearingDetails().stream()
            .filter(hearingBooking -> !sealedCmoHearingDateIds.contains(hearingBooking.getId()));
        if (excludePastDates) {
            hearingDetailsStream = hearingDetailsStream
                .filter(hearingBooking -> hearingBooking.getValue().startsAfterToday());
        }

        return hearingDetailsStream.map(this::buildDynamicListElement).collect(toList());
    }

    private Set<UUID> getSealedCmoHearingDateIds(CaseData caseData) {
        return caseData.getServedCaseManagementOrders()
            .stream()
            .map(e -> e.getValue().getId())
            .collect(toSet());
    }

    private DynamicListElement buildDynamicListElement(Element<HearingBooking> element) {
        return DynamicListElement.builder()
            .label(formatLocalDateToString(element.getValue().getStartDate().toLocalDate(), FormatStyle.MEDIUM))
            .code(element.getId())
            .build();
    }

    private DynamicListElement getPreselectedDate(List<DynamicListElement> list, UUID id) {
        return list.stream()
            .filter(item -> item.getCode().equals(id))
            .findFirst()
            .orElse(DynamicListElement.EMPTY);
    }
}
