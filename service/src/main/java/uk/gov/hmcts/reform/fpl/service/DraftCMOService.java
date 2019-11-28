package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;

@Service
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService, ObjectMapper mapper,
                           DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
        this.directionHelperService = directionHelperService;
    }

    public Map<String, Object> extractIndividualCaseManagementOrderObjects(
        CaseManagementOrder caseManagementOrder,
        List<Element<HearingBooking>> hearingDetails) {

        if (isNull(caseManagementOrder)) {
            caseManagementOrder = CaseManagementOrder.builder().build();
        }

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> reviewCaseManagementOrder = new HashMap<>();

        reviewCaseManagementOrder.put("cmoStatus", caseManagementOrder.getCmoStatus());

        data.put("cmoHearingDateList", getHearingDateDynamicList(hearingDetails, caseManagementOrder));
        data.put("schedule", caseManagementOrder.getSchedule());
        data.put("recitals", caseManagementOrder.getRecitals());
        data.put("reviewCaseManagementOrder", reviewCaseManagementOrder);

        return data;
    }

    public CaseManagementOrder getCaseManagementOrder(Map<String, Object> caseData) {
        DynamicList list = mapper.convertValue(caseData.get("cmoHearingDateList"), DynamicList.class);
        Map<String, Object> reviewCaseManagementOrder = mapper.convertValue(
            caseData.get("reviewCaseManagementOrder"), new TypeReference<>() {});
        CMOStatus cmoStatus = mapper.convertValue(reviewCaseManagementOrder.get("cmoStatus"), CMOStatus.class);
        Schedule schedule = mapper.convertValue(caseData.get("schedule"), Schedule.class);
        List<Element<Recital>> recitals = mapper.convertValue(caseData.get("recitals"), new TypeReference<>() {});

        return CaseManagementOrder.builder()
            .hearingDate(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .cmoStatus(cmoStatus)
            .schedule(schedule)
            .recitals(recitals)
            .build();
    }

    public void prepareCaseDetails(Map<String, Object> caseData, CaseManagementOrder caseManagementOrder) {
        final ImmutableSet<String> keysToRemove = ImmutableSet.of(
            "cmoHearingDateList",
            "schedule",
            "reviewCaseManagementOrder",
            "recitals");

        keysToRemove.forEach(caseData::remove);

        caseData.put("caseManagementOrder", caseManagementOrder);

        switch (caseManagementOrder.getCmoStatus()) {
            case SEND_TO_JUDGE:
                // Does the same as PARTIES_REVIEW for now but in the future this will change
            case PARTIES_REVIEW:
                caseData.put("shareableCMO", caseManagementOrder);
                break;
            case SELF_REVIEW:
                caseData.remove("shareableCMO");
                break;
            default:
                break;
        }
    }

    public void removeExistingCustomDirections(CaseDetails caseDetails) {
        caseDetails.getData().remove("allPartiesCustom");
        caseDetails.getData().remove("localAuthorityDirectionsCustom");
        caseDetails.getData().remove("cafcassDirectionsCustom");
        caseDetails.getData().remove("courtDirectionsCustom");
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

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }

    // Temporary, to be replaced by directionHelperService.combineAllDirections once all directions have been added
    private List<Element<Direction>> combineAllDirectionsForCMO(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(),
            LOCAL_AUTHORITY));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCafcassDirectionsCustom(),
            CAFCASS));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        return directions;
    }
}
