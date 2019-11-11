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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, DateFormatterService.class, DraftCMOService.class})
public class DraftCMOServiceTest {

    @Autowired
    private DraftCMOService draftCMOService;

    private final LocalDate date = LocalDate.now();

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date))
            )
            .build();

        DynamicList hearingList = draftCMOService.getHearingDatesDynamic(caseDetails);

        assertThat(hearingList.getListItems().get(0).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(5)));

        assertThat(hearingList.getListItems().get(1).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(2)));

        assertThat(hearingList.getListItems().get(2).getLabel())
            .isEqualTo(draftCMOService.convertDate(date));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {

        CaseDetails caseDetails = CaseDetails.builder()
            .data(ImmutableMap.of(
                "hearingDetails", createHearingBookings(date),
                "caseManagementOrder", createCaseManagementOrder(draftCMOService.convertDate(date.plusDays(2)))
                )).build();

        DynamicList hearingList = draftCMOService.getHearingDatesDynamic(caseDetails);

        assertThat(hearingList.getListItems().get(0).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(5)));

        assertThat(hearingList.getListItems().get(1).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(2)));

        assertThat(hearingList.getListItems().get(2).getLabel())
            .isEqualTo(draftCMOService.convertDate(date));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenHearingDatesNotNull() {
        DynamicList hearingList = draftCMOService.buildDynamicListFromHearingDetails(
            createHearingBookings(date));

        assertThat(hearingList.getListItems().get(0).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(5)));

        assertThat(hearingList.getListItems().get(1).getLabel())
            .isEqualTo(draftCMOService.convertDate(date.plusDays(2)));

        assertThat(hearingList.getListItems().get(2).getLabel())
            .isEqualTo(draftCMOService.convertDate(date));
    }

    @Test
    void convertDateShouldReturnDateInUKFormat() {
        String dateInUKFormat = draftCMOService.convertDate(LocalDate.of(2019,11,11));
        assertThat(dateInUKFormat).isEqualTo("11 Nov 2019");
    }

    private CaseManagementOrder createCaseManagementOrder(String hearingDate) {
        return CaseManagementOrder.builder()
            .hearingDate(hearingDate)
            .hearingDateId(UUID.fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2").toString()).build();
    }

    private List<Element<HearingBooking>> createHearingBookings(LocalDate now) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(now.plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .value(createHearingBooking(now.plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(now))
                .build()
        );
    }
}
