package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { DateFormatterService.class, CaseManageOrderActionService.class })
public class CaseManageOrderActionServiceTest {

    @Autowired
    private CaseManageOrderActionService caseManageOrderActionService;

    @Test
    void shouldFormatNextHearingBookingLabelWhenProvidedHearingBooking() {
        LocalDateTime date = LocalDateTime.of(2018, 2, 12, 9, 30);
        String label = caseManageOrderActionService.formatHearingBookingLabel(createHearingBooking(date, date));
        assertThat(label).isEqualTo(label).isEqualTo("The next hearing date is on 12 February at 9:30am");
    }
}
