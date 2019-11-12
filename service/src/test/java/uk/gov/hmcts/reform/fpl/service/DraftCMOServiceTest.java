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
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DateFormatterService.class, DraftCMOService.class})
class DraftCMOServiceTest {

    @Autowired
    private DraftCMOService draftCMOService;

    private final LocalDate date = LocalDate.now();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(
        FormatStyle.MEDIUM).localizedBy(Locale.UK);

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date))
            )
            .build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code("b15eb00f-e151-47f2-8e5f-374cc6fc2657")
                    .label(date.plusDays(5).format(dateTimeFormatter))
                    .build(),
                DynamicListElement.builder()
                    .code("6b3ee98f-acff-4b64-bb00-cc3db02a24b2")
                    .label(date.plusDays(2).format(dateTimeFormatter))
                    .build(),
                DynamicListElement.builder()
                    .code("ecac3668-8fa6-4ba0-8894-2114601a3e31")
                    .label(date.format(dateTimeFormatter))
                    .build()));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date),
                "caseManagementOrder", CaseManagementOrder.builder()
                    .hearingDate(date.plusDays(2).format(dateTimeFormatter))
                    .hearingDateId(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2").toString())
                    .build()
                )).build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getListItems())
            .contains(DynamicListElement.builder()
                    .code("6b3ee98f-acff-4b64-bb00-cc3db02a24b2")
                    .label(date.plusDays(2).format(dateTimeFormatter))
                    .build());
    }

    @Test
    void shouldReturnBlankHearingDateWhenPreviousSelectedValueNotMatchingDynamicListItems() {
        final String expectedHearDateId = randomUUID().toString();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date.plusDays(5)),
                "caseManagementOrder", CaseManagementOrder.builder()
                    .hearingDate(date.plusMonths(5).toString())
                    .hearingDateId(expectedHearDateId)
                    .build())
            ).build();

        DynamicList hearingList = draftCMOService.getHearingDateDynamicList(caseDetails);

        assertThat(hearingList.getListItems())
            .contains(DynamicListElement.builder()
                .code(expectedHearDateId)
                .label("")
                .build());
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
}
