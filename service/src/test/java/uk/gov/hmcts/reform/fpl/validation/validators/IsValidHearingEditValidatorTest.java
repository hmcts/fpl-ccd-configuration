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
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.EDIT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingOptions.VACATE_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class IsValidHearingEditValidatorTest extends AbstractValidationTest {
    @Autowired
    private Time time;

    private static final String ERROR_MESSAGE = "There are no relevant hearings to change.";

    @Nested
    class EditingHearings {
        @Test
        void shouldReturnErrorsWhenEditingAHearingButNoHearingsAreAvailable() {
            CaseData caseData = CaseData.builder().hearingOption(EDIT_HEARING).build();
            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);

            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnAnErrorWhenEditingAHearingButFutureHearingsAreNotAvailable() {
            List<Element<HearingBooking>> hearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now())
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().minusDays(1))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_HEARING)
                .hearingDetails(hearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldNotReturnAnErrorWhenEditingAHearingWithFutureHearingsAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(2))
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().plusDays(1))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(EDIT_HEARING)
                .hearingDetails(futureHearings)
                .build();

            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);
            assertThat(validationErrors).isEmpty();
        }
    }

    @Nested
    class AdjournHearings {
        @Test
        void shouldReturnErrorsWhenAdjourningAHearingButNoHearingsAreAvailable() {
            CaseData caseData = CaseData.builder().hearingOption(ADJOURN_HEARING).build();
            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);

            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnErrorsWhenAdjourningAHearingButNoPastHearingsAreAvailable() {
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
        void shouldReturnErrorsWhenVacatingAHearingButNoHearingsAreAvailable() {
            CaseData caseData = CaseData.builder().hearingOption(VACATE_HEARING).build();
            List<String> validationErrors = validate(caseData, HearingBookingGroup.class);

            assertThat(validationErrors).contains(ERROR_MESSAGE);
        }

        @Test
        void shouldReturnErrorsWhenVacatingAHearingButNoFutureHearingsAreAvailable() {
            List<Element<HearingBooking>> futureHearings = List.of(
                element(HearingBooking.builder()
                    .startDate(time.now().minusDays(1))
                    .build()),
                element(HearingBooking.builder()
                    .startDate(time.now().minusDays(2))
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .hearingOption(VACATE_HEARING)
                .hearingDetails(futureHearings)
                .build();

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
