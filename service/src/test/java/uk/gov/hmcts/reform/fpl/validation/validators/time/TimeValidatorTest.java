package uk.gov.hmcts.reform.fpl.validation.validators.time;

import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;

import java.time.LocalDateTime;

abstract class TimeValidatorTest extends AbstractValidationTest {
    static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(20);
    HearingBooking hearingBooking;
    Class<HearingBookingDetailsGroup> group = HearingBookingDetailsGroup.class;
}
