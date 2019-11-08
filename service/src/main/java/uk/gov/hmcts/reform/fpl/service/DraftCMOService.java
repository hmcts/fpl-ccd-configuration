package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicElementIndicator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class DraftCMOService {

    private final ObjectMapper mapper;
    private final DateFormatterService dateFormatterService;

    @Autowired
    public DraftCMOService(DateFormatterService dateFormatterService, ObjectMapper mapper) {
        this.mapper = mapper;
        this.dateFormatterService = dateFormatterService;
    }

    public DynamicList getHearingDatesDynamic(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDataMap, CaseData.class);

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

        DynamicList hearingDatesDynamic = buildDynamicListFromHearingDetails(hearingDetails);
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        prePopulateHearingDateSelection(hearingDetails, hearingDatesDynamic, caseManagementOrder);

        return hearingDatesDynamic;
    }

    private void prePopulateHearingDateSelection(
        List<Element<HearingBooking>> hearingDetails,
        DynamicList hearingDatesDynamic,
        CaseManagementOrder caseManagementOrder) {
        if (caseManagementOrder != null) {
            String hearingDateId = ObjectUtils.isEmpty(caseManagementOrder) ? ""
                : caseManagementOrder.getHearingDateId();
            if (!isEmpty(hearingDateId)) {
                // There was a previous hearing date therefore we need to remap it
                String date = hearingDetails.stream()
                    .filter(Objects::nonNull)
                    .filter(element -> element.getId().toString().equals(hearingDateId))
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
        }
    }

    public DynamicList buildDynamicListFromHearingDetails(List<Element<HearingBooking>> hearingDetails) {
        List<HearingDate> hearingDates = hearingDetails
            .stream()
            .map(element -> new HearingDate(element.getId(), element.getValue().getDate()))
            .collect(Collectors.toList());

        return DynamicList.toDynamicList(hearingDates, DynamicListElement.EMPTY);
    }

    public String convertDate(LocalDate date) {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }

    class HearingDate implements DynamicElementIndicator {

        private LocalDate date;
        private UUID id;

        HearingDate(UUID id, LocalDate date) {
            this.id = id;
            this.date = date;
        }

        @Override
        public DynamicListElement toDynamicElement() {
            final String dateString = convertDate(date);
            return DynamicListElement.builder().code(id.toString()).label(dateString).build();
        }
    }
}
