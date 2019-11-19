package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;
import uk.gov.hmcts.reform.fpl.validation.validators.ValidatorTest;

import java.time.LocalDateTime;

abstract class TimeValidatorTest extends ValidatorTest {
    static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(20);
    HearingBooking hearingBooking;
    Class<HearingBookingDetailsGroup> group = HearingBookingDetailsGroup.class;
}
