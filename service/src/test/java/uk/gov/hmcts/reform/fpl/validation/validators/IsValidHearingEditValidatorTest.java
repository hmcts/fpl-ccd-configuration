package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.validation.AbstractValidationTest;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_FUTURE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_PAST_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class IsValidHearingEditValidatorTest extends AbstractValidationTest {
    @Autowired
    private Time time;

    private static final String ERROR_MESSAGE = "There are no relevant hearings to change.";


    HearingBooking getTomorrowHearingBooking() {
        return HearingBooking.builder()
            .startDate(time.now().plusDays(1))
            .build();
    }

    HearingBooking getYesterdayHearingBooking() {
        return HearingBooking.builder()
            .startDate(time.now().minusDays(1))
            .build();
    }

    List<Element<HearingBooking>> getFutureHearings() {
        return List.of(
            element(getTomorrowHearingBooking())
        );
    }

    List<Element<HearingBooking>> getPastHearings() {
        return List.of(
            element(getYesterdayHearingBooking())
        );
    }

    List<Element<HearingBooking>> getFutureAndPastHearings() {
        return List.of(
            element(getYesterdayHearingBooking()),
            element(getTomorrowHearingBooking())
        );
    }

    @Nested
    class EditFutureHearings {

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingButHearingsAreNotAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingWithPastHearingAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingButHearingsAreNotAvailableHavingCancelledFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingWithPastHearingAndCancelledFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingButHearingsAreNotAvailableHavingCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingWithPastHearingsAndCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingFutureHearingButOnlyFutureHearingsAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingFutureHearingButOnlyFutureHearingsAvailableHavingCancelledFutureHearing()
        {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingFutureHearingButOnlyFutureHearingsAvailableHavingCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingFutureHearingWithPastAndFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .hearingDetails(getFutureAndPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldReturnAnErrorWhenEditingFutureHearingWithCurrentHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_FUTURE_HEARING)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .startDate(time.now())
                    .build())))
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }
    }

    @Nested
    class EditingPastHearings {
        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButHearingsAreNotAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingPastHearingWithPastHearingAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButHearingsAreNotAvailableHavingCancelledFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingPastHearingButHearingsWithPastHearingAndCancelledFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButHearingsAreNotAvailableHavingCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }


        @Test
        void shouldNotReturnAnErrorWhenEditingPastHearingWithPastHearingsAndCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .hearingDetails(getPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButOnlyFutureHearingsAvailable() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButOnlyFutureHearingsAvailableHavingCancelledFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getFutureHearings())
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingPastHearingButOnlyFutureHearingsAvailableHavingCancelledPastHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .cancelledHearingDetails(getPastHearings())
                .hearingDetails(getFutureHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingPastHearingWithPastAndFutureHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .hearingDetails(getFutureAndPastHearings())
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingPastHearingWithCurrentHearing() {
            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_PAST_HEARING)
                .hearingDetails(List.of(element(HearingBooking.builder()
                    .startDate(time.now())
                    .build())))
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }
    }

    @Nested
    class AdjournHearings {
        @Test
        void shouldReturnAnErrorWhenAdjourningAHearingButNoHearingsAreAvailable() {
            CaseData caseData = CaseData.builder().hearingOption(ADJOURN_HEARING).build();
            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);

            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenAdjourningAHearingButNoPastHearingsAreAvailable() {
            List<Element<HearingBooking>> hearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(3))
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(1))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(ADJOURN_HEARING)
                .hearingDetails(hearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenAdjourningAHearingAndPastHearingsAreAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now().minusDays(3))
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().minusDays(2))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(ADJOURN_HEARING)
                .hearingDetails(futureHearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenAdjourningAHearingAndCurrentHearingsAreAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now())
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(ADJOURN_HEARING)
                .hearingDetails(futureHearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }
    }

    @Nested
    class VacateHearings {
        @Test
        void shouldReturnAErrorWhenVacatingAHearingButNoHearingsAreAvailable() {
            CaseData caseData = CaseData.builder().hearingOption(VACATE_HEARING).build();
            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);

            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenVacatingAHearingAndFutureHearingsAreAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(1))
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(2))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(VACATE_HEARING)
                .hearingDetails(futureHearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }

        @Test
        void shouldNotReturnAnErrorWhenVacatingAHearingAndCurrentHearingsAreAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now())
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(VACATE_HEARING)
                .hearingDetails(futureHearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }
    }
}
