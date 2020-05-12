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

    public Document getOrder(CaseData caseData) {
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

    public DynamicList getHearingDateDynamicList(List<Element<HearingBooking>> hearings, CaseManagementOrder order) {
        List<DynamicListElement> values = getDateElements(hearings);

        DynamicListElement selectedValue = ofNullable(order)
            .map(x -> getPreselectedDate(values, x.getId()))
            .orElse(DynamicListElement.EMPTY);

        return DynamicList.builder()
            .listItems(values)
            .value(selectedValue)
            .build();
    }

    private List<DynamicListElement> getDateElements(List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .filter(hearingBooking -> hearingBooking.getValue().startsAfterToday())
            .map(this::buildDynamicListElement)
            .collect(toList());
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

    private void removeDirections(CaseDetails caseDetails) {
        Stream.of(Directions.class.getDeclaredFields()).forEach(field -> caseDetails.getData().remove(field.getName()));
    }

    private void addDirections(CaseDetails caseDetails, List<Element<Direction>> directions) {
        getMapping(directions)
            .forEach((key, value) -> caseDetails.getData().put(key.toCaseManagementOrderDirectionField(), value));
    }
}
