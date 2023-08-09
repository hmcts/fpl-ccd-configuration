package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, ResendCafcassEmailService.class})
class ResendCafcassEmailServiceTest {

    @Autowired
    private ResendCafcassEmailService resendCafcassEmailService;

    @Test
    void shouldFetch() {
        assertThat(resendCafcassEmailService.getAllCaseIds()).hasSize(1);
    }

    @Test
    void shouldReturnOrderDates() {
        assertThat(resendCafcassEmailService.getOrderDates(1234L)).contains(
            LocalDate.of(2022, 1, 1)
        );
    }

    @Test
    void shouldReturnHearingDateTimes() {
        assertThat(resendCafcassEmailService.getNoticeOfHearingDateTimes(1234L)).contains(
            LocalDateTime.of(2022, 1, 1, 10, 30)
        );
    }
}
