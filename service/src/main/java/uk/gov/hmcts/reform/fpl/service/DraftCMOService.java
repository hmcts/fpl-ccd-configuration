package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.HearingDateHelper;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class DraftCMOService {
    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService, ObjectMapper mapper) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
    }

    public DynamicList getHearingDateDynamicList(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);

        if (!isEmpty(caseData.getCaseManagementOrder())) {
            prePopulateHearingDateSelection(hearingDetails,
                hearingDatesDynamic,
                caseData.getCaseManagementOrder());
        }

        return hearingDatesDynamic;
    }

    private void prePopulateHearingDateSelection(List<Element<HearingBooking>> hearingDetails,
                                                 DynamicList hearingDatesDynamic,
                                                 CaseManagementOrder caseManagementOrder) {
        String hearingDateId = isEmpty(caseManagementOrder) ? "" : caseManagementOrder.getHearingDateId();
        // There was a previous hearing date therefore we need to remap it
        String date = hearingDetails.stream()
            .filter(Objects::nonNull)
            .filter(element -> element.getId().toString().equals(caseManagementOrder.getHearingDateId()))
            .findFirst()
            .map(element -> convertDate(element.getValue().getDate()))
            .orElse("");

        DynamicListElement listElement = DynamicListElement.builder()
            .label(date)
            .code(hearingDateId)
            .build();

        hearingDatesDynamic.setValue(listElement);
        if (!hearingDatesDynamic.getListItems().contains(listElement)) {
            hearingDatesDynamic.getListItems().add(listElement);
        }
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDateHelper> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDateHelper(element.getId(), element.getValue().getDate(), dateFormatterService))
            .collect(toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    private String convertDate(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
