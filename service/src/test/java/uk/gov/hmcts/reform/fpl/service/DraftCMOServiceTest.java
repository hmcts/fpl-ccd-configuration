package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DateFormatterService.class, DraftCMOService.class,
    DirectionHelperService.class})
class DraftCMOServiceTest {

    @Autowired
    private DraftCMOService draftCMOService;

    @Autowired
    private DateFormatterService dateFormatterService;

    private final LocalDate date = LocalDate.now();

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("hearingDetails", createHearingBookings(date)))
            .build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(2))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(0))
                    .build()));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date),
                "caseManagementOrder", CaseManagementOrder.builder()
                    .hearingDate(formatLocalDateToMediumStyle(2))
                    .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .build()
            )).build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldReturnCaseManagementOrderWhenProvidedCaseDetails() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("cmoHearingDateList", getDynamicList()))
            .build();

        CaseManagementOrder caseManagementOrder = draftCMOService.getCaseManagementOrder(caseDetails);

        assertThat(caseManagementOrder).isNotNull()
            .extracting("id", "hearingDate").containsExactly(
            fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
            formatLocalDateToMediumStyle(5));
    }

    @Test
    void shouldFormatRespondentsIntoKeyWhenRespondentsArePresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("respondents1", createRespondents()))
            .build();

        String respondentsKey = draftCMOService.createParentsAndRespondentAssigneeDropdownKey(caseDetails);

        assertThat(respondentsKey).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson");
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentsAreNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();

        String respondentsKey = draftCMOService.createParentsAndRespondentAssigneeDropdownKey(caseDetails);

        assertThat(respondentsKey).isEqualTo("");
    }

    @Test
    void shouldFormatOthersIntoKeyWhenOthersArePresent() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of("others", createOthers())).build();

        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(caseDetails);

        assertThat(othersKey).contains(
            "Person 1 - Kyle Stafford",
            "Other Person 2 - Sarah Simpson");
    }

    @Test
    void shouldReturnEmptyStringWhenOthersAreNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of())
            .build();

        String othersKey = draftCMOService.createParentsAndRespondentAssigneeDropdownKey(caseDetails);

        assertThat(othersKey).isEqualTo("");
    }

    private DynamicList getDynamicList() {
        DynamicList dynamicList = draftCMOService.buildDynamicListFromHearingDetails(createHearingBookings(date));

        DynamicListElement listElement = DynamicListElement.builder()
            .label(formatLocalDateToMediumStyle(5))
            .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
            .build();

        dynamicList.setValue(listElement);
        return dynamicList;
    }

    private List<Element<HearingBooking>> createHearingBookings(LocalDate now) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(createHearingBooking(now.plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .value(createHearingBooking(now.plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .value(createHearingBooking(now))
                .build()
        );
    }

    private String formatLocalDateToMediumStyle(int i) {
        return dateFormatterService.formatLocalDateToString(date.plusDays(i), FormatStyle.MEDIUM);
    }
}
