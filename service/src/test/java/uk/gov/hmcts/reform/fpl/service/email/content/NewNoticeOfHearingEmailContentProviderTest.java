package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {NewNoticeOfHearingEmailContentProvider.class, LookupTestConfig.class, HearingBookingService.class, FixedTimeConfiguration.class})
class NewNoticeOfHearingEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;

    @Disabled
    @Test
    void shouldReturnExpectedMapWithValidHearingContent() {

        LocalDateTime hearingDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("hearingDetails", wrapElements(createHearingBooking(hearingDate, hearingDate.plusDays(1)))))
            .build();

        assertThat(newNoticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(populatedCaseDetails(), HearingBooking.builder().build())).isEqualTo(expectedMap());
    }

    private NewNoticeOfHearingTemplate expectedMap() {
        return NewNoticeOfHearingTemplate.builder()
            .hearingVenue("testVenue")
            .build();
    }
}
