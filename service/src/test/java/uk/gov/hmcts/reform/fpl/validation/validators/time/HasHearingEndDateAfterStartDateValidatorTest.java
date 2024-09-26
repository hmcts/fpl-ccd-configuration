package uk.gov.hmcts.reform.fpl.validation.validators.time;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingEndDateGroup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasHearingEndDateAfterStartDateValidatorTest extends TimeValidatorTest {

    private Validator validator;
    private ValidateGroupService validateGroupService;

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validateGroupService = new ValidateGroupService(validator);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHearingEndDatesSource")
    void shouldReturnAnErrorWhenHearingEndTimeIsBeforeStartTime(
        String name, LocalDateTime startTime, LocalDateTime endTime) {

        CaseData caseData = CaseData.builder()
            .hearingStartDate(startTime)
            .hearingEndDateTime(endTime)
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
            .hearingEndDateTime(endTime)
            .build();

        List<String> errorMessages = validateGroupService.validateGroup(caseData, HearingEndDateGroup.class);
        assertThat(errorMessages).isEmpty();
    }

    private static Stream<Arguments> invalidHearingEndDatesSource() {
        LocalDateTime startTime = LocalDateTime.now();
        return Stream.of(
            Arguments.of("Hearing end date is before start date", startTime, startTime.minusDays(1)),
            Arguments.of("Hearing end time is 1 second before start time", startTime, startTime.minusSeconds(1)),
            Arguments.of("Hearing end time is 1 minute before start time", startTime, startTime.minusMinutes(1)),
            Arguments.of("Hearing end date time and start date time are same", startTime, startTime)
        );
    }

    private static Stream<Arguments> validHearingEndDatesSource() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
            Arguments.of("Hearing start date is null", null, now),
            Arguments.of("Hearing end time is null", now, null),
            Arguments.of("Hearing start and end time are null", null, null),
            Arguments.of("Hearing end time is after the start time", now, now.plusSeconds(1))
        );
    }

}
