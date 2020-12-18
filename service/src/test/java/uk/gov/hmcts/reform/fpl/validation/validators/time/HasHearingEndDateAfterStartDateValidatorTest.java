package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HasHearingEndDateAfterStartDateValidatorTest extends TimeValidatorTest {

    @SpyBean
    private ValidateGroupService validateGroupService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHearingEndDatesSource")
    void shouldReturnAnErrorWhenHearingEndTimeIsBeforeStartTime(
        String name, LocalDateTime startTime, LocalDateTime endTime) {

        CaseData caseData = CaseData.builder()
            .hearingStartDate(startTime)
            .hearingEndDate(endTime)
            .build();

        List<String> errorMessages = validateGroupService.validateGroup(caseData, HearingEndDateGroup.class);
        assertThat(errorMessages).containsExactly("The end date and time must be after the start date and time");
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("validHearingEndDatesSource")
    void shouldNotReturnErrorWhenHearingEndTimeIsAfterStartTime(
        String name, LocalDateTime startTime, LocalDateTime endTime) {

        CaseData caseData = CaseData.builder()
            .hearingStartDate(startTime)
            .hearingEndDate(endTime)
            .build();

        List<String> errorMessages = validateGroupService.validateGroup(caseData, HearingEndDateGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    private static Stream<Arguments> invalidHearingEndDatesSource() {
        LocalDateTime startDateTime = LocalDateTime.now();
        return Stream.of(
            Arguments.of("Hearing end date is before start date", startDateTime, startDateTime.minusDays(1)),
            Arguments.of("Hearing end time is before start time", startDateTime, startDateTime.minusHours(1)),
            Arguments.of("Hearing end date time and start date time are same", startDateTime, startDateTime)
        );
    }

    private static Stream<Arguments> validHearingEndDatesSource() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
            Arguments.of("Hearing start date is null", null, now),
            Arguments.of("Hearing end time is null", now, null),
            Arguments.of("Hearing start and end time are null", null, null),
            Arguments.of("Hearing end time is after the start time", now, now.plusMinutes(2))
        );
    }

}
