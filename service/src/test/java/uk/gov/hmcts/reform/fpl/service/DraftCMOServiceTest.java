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
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createElementCollection;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createOthers;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DateFormatterService.class, DraftCMOService.class,
    DirectionHelperService.class})
class DraftCMOServiceTest {
    private final LocalDateTime date = LocalDateTime.now();

    @Autowired
    private DraftCMOService draftCMOService;

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private DirectionHelperService directionHelperService;

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date))).build();

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
                    .build())).build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldReturnCaseManagementOrderWhenProvidedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            caseData.put(direction.getValue() + "Custom", createElementCollection(createUnassignedDirection()))
        );

        caseData.put("cmoHearingDateList", getDynamicList());

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseDetails);

        assertThat(caseManagementOrder).isNotNull()
            .extracting("id", "hearingDate").containsExactly(
            fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
            formatLocalDateToMediumStyle(5));

        assertThat(caseManagementOrder.getDirections()).isEqualTo(createCmoDirections());
    }

    @Test
    void shouldRemoveCustomDirectionsWhenPresentInCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(DirectionAssignee.values()).forEach(direction ->
            caseData.put(direction.getValue() + "Custom", createElementCollection(createUnassignedDirection()))
        );

        CaseDetails caseDetails = CaseDetails.builder().data(caseData).build();

        draftCMOService.removeExistingCustomDirections(caseDetails);

        assertThat(caseDetails.getData()).doesNotContainKey("allPartiesCustom");
        assertThat(caseDetails.getData()).doesNotContainKey("localAuthorityDirectionsCustom");
        assertThat(caseDetails.getData()).doesNotContainKey("cafcassDirectionsCustom");
        assertThat(caseDetails.getData()).doesNotContainKey("courtDirectionsCustom");
    }

    @Test
    void shouldFormatRespondentsIntoKeyWhenRespondentsArePresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of("respondents1", createRespondents()))
            .build();

        String respondentsKey = draftCMOService.createRespondentAssigneeDropdownKey(caseDetails);

        assertThat(respondentsKey).contains(
            "Respondent 1 - Timothy Jones",
            "Respondent 2 - Sarah Simpson");
    }

    @Test
    void shouldReturnEmptyStringWhenRespondentsAreNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of()).build();

        String respondentsKey = draftCMOService.createRespondentAssigneeDropdownKey(caseDetails);

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
    void shouldIncludeEmptyStatePlaceholderWhenOthersDoesNotIncludeFullName() {
        CaseDetails caseDetails = CaseDetails.builder().data(ImmutableMap.of("others", Others.builder()
            .firstOther(Other.builder()
                .DOB("02/05/1988")
                .build())
            .build())).build();

        String othersKey = draftCMOService.createOtherPartiesAssigneeDropdownKey(caseDetails);

        assertThat(othersKey).contains("Person 1 - " + EMPTY_PLACEHOLDER);
    }

    @Test
    void shouldReturnEmptyStringWhenOthersAreNotPresentOnCaseData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of())
            .build();

        String othersKey = draftCMOService.createRespondentAssigneeDropdownKey(caseDetails);

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

    private List<Element<HearingBooking>> createHearingBookings(LocalDateTime now) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                .value(createHearingBooking(now.plusDays(5), now.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .value(createHearingBooking(now.plusDays(2), now.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .value(createHearingBooking(now, now.plusDays(1)))
                .build());
    }

    private String formatLocalDateToMediumStyle(int i) {
        return dateFormatterService.formatLocalDateToString(date.plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
    }
}
