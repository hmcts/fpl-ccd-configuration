package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.service.CommonDirectionService.assignCustomDirections;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseManagementOrderService {
    private final CommonDirectionService directionService;
    private final Time time;
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

    public void prepareCustomDirections(CaseDetails caseDetails, CaseManagementOrder order) {
        if (!isNull(order)) {
            directionService.sortDirectionsByAssignee(order.getDirections())
                .forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));
        } else {
            removeExistingCustomDirections(caseDetails);
        }
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

    //TODO: isAfter method in hearing booking does not filter out todays date as Time is set before LocalDate.now()
    // is evaluated
    private List<DynamicListElement> getDateElements(List<Element<HearingBooking>> hearings) {
        return hearings.stream()
            .filter(hearingBooking -> hearingBooking.getValue().getStartDate().isAfter(time.now()))
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

    private void removeExistingCustomDirections(CaseDetails caseDetails) {
        caseDetails.getData().remove("allPartiesCustomCMO");
        caseDetails.getData().remove("localAuthorityDirectionsCustomCMO");
        caseDetails.getData().remove("cafcassDirectionsCustomCMO");
        caseDetails.getData().remove("courtDirectionsCustomCMO");
        caseDetails.getData().remove("respondentDirectionsCustomCMO");
        caseDetails.getData().remove("otherPartiesDirectionsCustomCMO");
    }

    private List<Element<Direction>> combineAllDirectionsForCmo(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(assignCustomDirections(caseData.getAllPartiesCustomCMO(),
            ALL_PARTIES));

        directions.addAll(assignCustomDirections(caseData.getLocalAuthorityDirectionsCustomCMO(),
            LOCAL_AUTHORITY));

        directions.addAll(orderByParentsAndRespondentAssignee(assignCustomDirections(
            caseData.getRespondentDirectionsCustomCMO(), PARENTS_AND_RESPONDENTS)));

        directions.addAll(assignCustomDirections(caseData.getCafcassDirectionsCustomCMO(),
            CAFCASS));

        directions.addAll(orderByOtherPartiesAssignee(assignCustomDirections(
            caseData.getOtherPartiesDirectionsCustomCMO(), OTHERS)));

        directions.addAll(assignCustomDirections(caseData.getCourtDirectionsCustomCMO(), COURT));

        return directions;
    }

    private List<Element<Direction>> orderByParentsAndRespondentAssignee(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue()
            .getParentsAndRespondentsAssignee()
            .ordinal()));

        return directions;
    }

    private List<Element<Direction>> orderByOtherPartiesAssignee(List<Element<Direction>> directions) {
        directions.sort(comparingInt(direction -> direction.getValue()
            .getOtherPartiesAssignee()
            .ordinal()));

        return directions;
    }
}
