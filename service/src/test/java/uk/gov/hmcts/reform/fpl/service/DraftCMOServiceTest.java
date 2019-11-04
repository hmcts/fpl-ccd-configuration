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
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DateFormatterService.class, DraftCMOService.class})
public class DraftCMOServiceTest {

    @Autowired
    private DateFormatterService dateFormatterService;

    @Autowired
    private DraftCMOService service;

    @Test
    void shouldReturnHearingDateDynamicListIfHearingDatesNotNull() {
        DynamicList hearingList = service.makeHearingDateList(createHearingBookings());

        assertThat(hearingList.getListItems().get(0).getCode()).isEqualTo(convertdateTopLocalFormat(LocalDate.now().plusDays(5)));
        assertThat(hearingList.getListItems().get(1).getCode()).isEqualTo(convertdateTopLocalFormat(LocalDate.now().plusDays(2)));
        assertThat(hearingList.getListItems().get(2).getCode()).isEqualTo(convertdateTopLocalFormat(LocalDate.now()));
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(5)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(2)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now()))
                .build()
        );
    }

    private String convertdateTopLocalFormat(LocalDate date)
    {
        return dateFormatterService.formatLocalDateToString(date, FormatStyle.MEDIUM);
    }
}
