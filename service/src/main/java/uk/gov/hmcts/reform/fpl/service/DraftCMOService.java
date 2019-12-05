package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.configuration.ReviewCaseManagementOrder;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;

    public Map<String, Object> extractIndividualCaseManagementOrderObjects(
        CaseManagementOrder caseManagementOrder,
        List<Element<HearingBooking>> hearingDetails) {

        if (isNull(caseManagementOrder)) {
            caseManagementOrder = CaseManagementOrder.builder().build();
        }

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> reviewCaseManagementOrder = new HashMap<>();

        reviewCaseManagementOrder.put("cmoStatus", caseManagementOrder.getCmoStatus());
        reviewCaseManagementOrder.put("orderDoc", caseManagementOrder.getOrderDoc());

        data.put("cmoHearingDateList", getHearingDateDynamicList(hearingDetails, caseManagementOrder));
        data.put("schedule", caseManagementOrder.getSchedule());
        data.put("recitals", caseManagementOrder.getRecitals());
        data.put("reviewCaseManagementOrder", reviewCaseManagementOrder);

        return data;
    }

    public CaseManagementOrder prepareCMO(CaseData caseData) {
        Optional<DynamicList> cmoHearingDateList = Optional.ofNullable(caseData.getCmoHearingDateList());
        Optional<ReviewCaseManagementOrder> optionalReview = Optional.ofNullable(
            caseData.getReviewCaseManagementOrder());

        return CaseManagementOrder.builder()
            .hearingDate(cmoHearingDateList.map(DynamicList::getValueLabel).orElse(null))
            .id(cmoHearingDateList.map(DynamicList::getValueCode).orElse(null))
            .directions(combineAllDirectionsForCmo(caseData))
            .schedule(caseData.getSchedule())
            .recitals(caseData.getRecitals())
            .cmoStatus(optionalReview.map(ReviewCaseManagementOrder::getCmoStatus).orElse(null))
            .orderDoc(optionalReview.map(ReviewCaseManagementOrder::getOrderDoc).orElse(null))
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
                caseData.put("sharedDraftCMO", caseManagementOrder);
                break;
            case SELF_REVIEW:
                caseData.remove("sharedDraftCMO");
                break;
            default:
                break;
        }
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

    public String createRespondentAssigneeDropdownKey(List<Element<Respondent>> respondents) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < respondents.size(); i++) {
            RespondentParty respondentParty = respondents.get(i).getValue().getParty();

            String key = String.format("Respondent %d - %s", i + 1, respondentParty.getFullName());
            stringBuilder.append(key).append("\n\n");
        }

        return stringBuilder.toString().stripTrailing();
    }

    public String createOtherPartiesAssigneeDropdownKey(Others others) {
        StringBuilder stringBuilder = new StringBuilder();

        if (isNotEmpty(others)) {
            for (int i = 0; i < others.getAllOthers().size(); i++) {
                Other other = others.getAllOthers().get(i);
                String key;

                if (i == 0) {
                    key = String.format("Person 1 - %s", defaultIfNull(other.getName(), EMPTY_PLACEHOLDER));
                } else {
                    key = String.format("Other Person %d - %s", i,
                        defaultIfNull(other.getName(), EMPTY_PLACEHOLDER));
                }

                stringBuilder.append(key).append("\n\n");
            }
        }

        return stringBuilder.toString().stripTrailing();
    }

    public void prepareCustomDirections(Map<String, Object> data) {
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        if (!isNull(caseData.getCaseManagementOrder())) {
            directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections())
                .forEach((key, value) -> data.put(key.getValue(), value));
        } else {
            removeExistingCustomDirections(data);
        }
    }


    private void removeExistingCustomDirections(Map<String, Object> caseData) {
        caseData.remove("allPartiesCustom");
        caseData.remove("localAuthorityDirectionsCustom");
        caseData.remove("cafcassDirectionsCustom");
        caseData.remove("courtDirectionsCustom");
        caseData.remove("respondentDirectionsCustom");
        caseData.remove("otherPartiesDirectionsCustom");
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
        directions.sort(Comparator.comparingInt(direction -> direction.getValue()
            .getParentsAndRespondentsAssignee()
            .ordinal()));

        return directions;
    }

    private List<Element<Direction>> orderByOtherPartiesAssignee(List<Element<Direction>> directions) {
        directions.sort(Comparator.comparingInt(direction -> direction.getValue()
            .getOtherPartiesAssignee()
            .ordinal()));

        return directions;
    }

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
