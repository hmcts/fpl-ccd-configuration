package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;
    private final DirectionHelperService directionHelperService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService,
                           ObjectMapper mapper,
                           DirectionHelperService directionHelperService) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
        this.directionHelperService = directionHelperService;
    }

    public DynamicList getHearingDateDynamicList(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);

        if (isNotEmpty(caseData.getCaseManagementOrder())) {
            prePopulateHearingDateSelection(hearingDetails,
                hearingDatesDynamic,
                caseData.getCaseManagementOrder());
        }

        return hearingDatesDynamic;
    }

    public String createRespondentAssigneeDropdownKey(List<Element<Respondent>> respondents) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < respondents.size(); i++) {
            RespondentParty respondentParty = respondents.get(i).getValue().getParty();

            String key = String.format("Respondent %d - %s", i + 1, getRespondentFullName(respondentParty));
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
                    key = String.format("Other person %d - %s", i,
                        defaultIfNull(other.getName(), EMPTY_PLACEHOLDER));
                }

                stringBuilder.append(key).append("\n\n");
            }
        }

        return stringBuilder.toString().stripTrailing();
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateDynamicElement> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDateDynamicElement(
                formatLocalDateToMediumStyle(element.getValue().getStartDate().toLocalDate()), element.getId()))
            .collect(toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    public CaseManagementOrder prepareCMO(CaseDetails caseDetails) {
        DynamicList list = mapper.convertValue(caseDetails.getData().get("cmoHearingDateList"), DynamicList.class);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return CaseManagementOrder.builder()
            .hearingDate(list.getValue().getLabel())
            .id(list.getValue().getCode())
            .directions(combineAllDirectionsForCmo(caseData))
            .build();
    }

    public void prepareCustomDirections(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (!isNull(caseData.getCaseManagementOrder())) {
            directionHelperService.sortDirectionsByAssignee(caseData.getCaseManagementOrder().getDirections())
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

    private String getRespondentFullName(RespondentParty respondentParty) {
        String firstName = defaultIfNull(respondentParty.getFirstName(), "");
        String lastName = defaultIfNull(respondentParty.getLastName(), "");

        return String.format("%s %s", firstName, lastName);
    }

    private List<Element<Direction>> combineAllDirectionsForCmo(CaseData caseData) {
        List<Element<Direction>> directions = new ArrayList<>();

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getAllPartiesCustom(), ALL_PARTIES));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getLocalAuthorityDirectionsCustom(),
            LOCAL_AUTHORITY));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCafcassDirectionsCustom(),
            CAFCASS));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getCourtDirectionsCustom(), COURT));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getRespondentDirectionsCustom(),
            PARENTS_AND_RESPONDENTS));

        directions.addAll(directionHelperService.assignCustomDirections(caseData.getOtherPartiesDirectionsCustom(),
            OTHERS));

        return directions;
    }

    private String formatLocalDateToMediumStyle(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
