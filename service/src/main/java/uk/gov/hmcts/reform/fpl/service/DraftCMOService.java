package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;

//TODO: methods to be moved to CaseManagementOrderService and DirectionHelperService.
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftCMOService {
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;

    public Map<String, Object> extractIndividualCaseManagementOrderObjects(
        CaseManagementOrder caseManagementOrder,
        List<Element<HearingBooking>> hearingDetails) {

        if (isNull(caseManagementOrder)) {
            caseManagementOrder = CaseManagementOrder.builder().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("cmoHearingDateList", getHearingDateDynamicList(hearingDetails, caseManagementOrder));
        data.put("schedule", caseManagementOrder.getSchedule());
        data.put("recitals", caseManagementOrder.getRecitals());

        return data;
    }

    public CaseManagementOrder prepareCMO(CaseData caseData, CaseManagementOrder order) {
        Optional<CaseManagementOrder> oldCMO = Optional.ofNullable(order);
        Optional<DynamicList> cmoHearingDateList = Optional.ofNullable(caseData.getCmoHearingDateList());

        return CaseManagementOrder.builder()
            .hearingDate(cmoHearingDateList.map(DynamicList::getValueLabel).orElse(null))
            .id(cmoHearingDateList.map(DynamicList::getValueCode).orElse(null))
            .directions(combineAllDirectionsForCmo(caseData))
            .schedule(caseData.getSchedule())
            .recitals(caseData.getRecitals())
            .status(oldCMO.map(CaseManagementOrder::getStatus).orElse(null))
            .orderDoc(oldCMO.map(CaseManagementOrder::getOrderDoc).orElse(null))
            .action(oldCMO.map(CaseManagementOrder::getAction).orElse(null))
            .build();
    }

    public void removeTransientObjectsFromCaseData(Map<String, Object> caseData) {
        final Set<String> keysToRemove = Set.of(
            "cmoHearingDateList",
            "schedule",
            "recitals");

        keysToRemove.forEach(caseData::remove);
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateDynamicElement> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDateDynamicElement(
                formatLocalDateToMediumStyle(element.getValue().getStartDate().toLocalDate()), element.getId()))
            .collect(toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    private DynamicList getHearingDateDynamicList(List<Element<HearingBooking>> hearingDetails,
                                                  CaseManagementOrder caseManagementOrder) {
        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);

        if (isNotEmpty(caseManagementOrder)) {
            prePopulateHearingDateSelection(hearingDetails, hearingDatesDynamic, caseManagementOrder);
        }

        return hearingDatesDynamic;
    }

    public void prepareCustomDirections(CaseDetails caseDetails, CaseManagementOrder order) {
        if (!isNull(order)) {
            directionHelperService.sortDirectionsByAssignee(order.getDirections())
                .forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));
        } else {
            removeExistingCustomDirections(caseDetails);
        }
    }

    private void removeExistingCustomDirections(CaseDetails caseDetails) {
        caseDetails.getData().remove("allPartiesCustom");
        caseDetails.getData().remove("localAuthorityDirectionsCustom");
        caseDetails.getData().remove("cafcassDirectionsCustom");
        caseDetails.getData().remove("courtDirectionsCustom");
        caseDetails.getData().remove("respondentDirectionsCustom");
        caseDetails.getData().remove("otherPartiesDirectionsCustom");
    }

    private void prePopulateHearingDateSelection(List<Element<HearingBooking>> hearingDetails,
                                                 DynamicList hearingDatesDynamic,
                                                 CaseManagementOrder caseManagementOrder) {
        UUID hearingDateId = caseManagementOrder.getId();
        // There was a previous hearing date therefore we need to remap it
        String date = hearingDetails.stream()
            .filter(Objects::nonNull)
            .filter(element -> element.getId().equals(caseManagementOrder.getId()))
            .findFirst()
            .map(element -> formatLocalDateToMediumStyle(element.getValue().getStartDate().toLocalDate()))
            .orElse("");

        DynamicListElement listElement = DynamicListElement.builder()
            .label(date)
            .code(hearingDateId)
            .build();

        hearingDatesDynamic.setValue(listElement);
    }

    private List<Element<Direction>> combineAllDirectionsForCmo(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(),
            LOCAL_AUTHORITY));

        directions.addAll(orderByParentsAndRespondentAssignee(directionHelperService.assignCustomDirections(
            caseData.getRespondentDirectionsCustom(), PARENTS_AND_RESPONDENTS)));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCafcassDirectionsCustom(),
            CAFCASS));

        directions.addAll(orderByOtherPartiesAssignee(directionHelperService.assignCustomDirections(
            caseData.getOtherPartiesDirectionsCustom(), OTHERS)));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

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

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
