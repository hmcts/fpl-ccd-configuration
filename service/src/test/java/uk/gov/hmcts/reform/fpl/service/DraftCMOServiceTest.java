package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DateFormatterService.class, DraftCMOService.class})
public class DraftCMOServiceTest {

    @Autowired
    private DraftCMOService draftCMOService;

    @Test
    void shouldReturnHearingDateDynamicListIfHearingDatesNotNull() {
        LocalDate now = LocalDate.now();
        DynamicList hearingList = draftCMOService.buildDynamicListFromHearingDetails(
            createHearingBookings(now));

        assertThat(hearingList.getListItems().get(0).getCode())
            .isEqualTo(draftCMOService.convertDate(now.plusDays(5)));

        assertThat(hearingList.getListItems().get(1).getCode())
            .isEqualTo(draftCMOService.convertDate(now.plusDays(2)));

        assertThat(hearingList.getListItems().get(2).getCode())
            .isEqualTo(draftCMOService.convertDate(now));
    }

    private List<Element<HearingBooking>> createHearingBookings(LocalDate now) {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(now.plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(now.plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(now))
                .build()
        );
    }
}
