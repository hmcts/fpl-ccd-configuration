package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.DISTRICT_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService.EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.service.CommonCaseDataExtractionService.HEARING_EMPTY_PLACEHOLDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createJudgeAndLegalAdvisor;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CommonCaseDataExtractionService.class, DateFormatterService.class,
    HearingVenueLookUpService.class
})
class CommonCaseDataExtractionServiceTest {
    private final CommonCaseDataExtractionService commonCaseDataExtractionService;
    private HearingBooking hearingBooking;

    @Autowired
    CommonCaseDataExtractionServiceTest(
        CommonCaseDataExtractionService commonCaseDataExtractionService) {
        this.commonCaseDataExtractionService = commonCaseDataExtractionService;
    }

    @Test
    void shouldReturnTheFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final Optional<String> hearingDate = commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
            hearingBooking);

        assertThat(hearingDate.isEmpty());
    }

    @Test
    void shouldReturnAnEmptyStringWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final Optional<String> hearingDate = commonCaseDataExtractionService.getHearingDateIfHearingsOnSameDay(
            hearingBooking);

        assertThat(hearingDate.orElse("")).isEqualTo("11 December 2020");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWithDatesWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String hearingTime = commonCaseDataExtractionService.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("11 December, 3:30pm - 12 December, 4:30pm");
    }

    @Test
    void shouldReturnTheFormattedTimeRangeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String hearingTime = commonCaseDataExtractionService.getHearingTime(hearingBooking);

        assertThat(hearingTime).isEqualTo("3:30pm - 4:30pm");
    }

    @Test
    void shouldReturnAFormattedDateWhenStartAndEndDateAreNotTheSame() {
        hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final String prehearingAttendance = commonCaseDataExtractionService.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("11 December 2020, 2:30pm");
    }

    @Test
    void shouldReturnAFormattedTimeWhenStartAndEndDateAreTheSame() {
        hearingBooking = createHearingBookingWithTimesOnSameDay();

        final String prehearingAttendance = commonCaseDataExtractionService.extractPrehearingAttendance(hearingBooking);

        assertThat(prehearingAttendance).isEqualTo("2:30pm");
    }

    @Test
    void shouldReturnAMapOfEmptyPlaceholdersWhenHearingBookingIsNull() {
        final Map<String, Object> hearingBookingData = commonCaseDataExtractionService.getHearingBookingData(null);

        assertThat(hearingBookingData).isEqualTo(ImmutableMap.of(
            "hearingDate", HEARING_EMPTY_PLACEHOLDER,
            "hearingVenue", HEARING_EMPTY_PLACEHOLDER,
            "preHearingAttendance", HEARING_EMPTY_PLACEHOLDER,
            "hearingTime", HEARING_EMPTY_PLACEHOLDER
        ));
    }

    @Test
    void shouldReturnAMapWithAPopulatedHearingDateWhenHearingBookingIsNotNull() {
        final HearingBooking hearingBooking = createHearingBookingWithTimesOnSameDay();

        final Map<String, Object> hearingBookingData = commonCaseDataExtractionService
            .getHearingBookingData(hearingBooking);

        assertThat(hearingBookingData.get("hearingDate")).isEqualTo("11 December 2020");
        assertThat(hearingBookingData.get("hearingVenue")).asString().startsWith("Crown Building");
        assertThat(hearingBookingData.get("preHearingAttendance")).isEqualTo("2:30pm");
        assertThat(hearingBookingData.get("hearingTime")).isEqualTo("3:30pm - 4:30pm");
    }

    @Test
    void shouldReturnAMapWithoutAPopulatedHearingDateWhenHearingBookingIsNotNull() {
        final HearingBooking hearingBooking = createHearingBookingWithTimesOnDifferentDays();

        final Map<String, Object> hearingBookingData = commonCaseDataExtractionService
            .getHearingBookingData(hearingBooking);

        assertThat(hearingBookingData.get("hearingVenue")).asString().startsWith("Crown Building");
        assertThat(hearingBookingData.get("preHearingAttendance")).isEqualTo("11 December 2020, 2:30pm");
        assertThat(hearingBookingData.get("hearingTime")).isEqualTo("11 December, 3:30pm - 12 December, 4:30pm");
    }

    @Test
    void shouldReturnAMapWithTheFormattedJudgeAndLegalAdvisorNamesPopulatedWhenObjectIsPopulated() {
        final JudgeAndLegalAdvisor judgeAndLegalAdvisor = createJudgeAndLegalAdvisor(
            "legal", "full name", "last name", DISTRICT_JUDGE);

        final Map<String, Object> judgeAndLegalAdvisorData = commonCaseDataExtractionService
            .getJudgeAndLegalAdvisorData(judgeAndLegalAdvisor);

        assertThat(judgeAndLegalAdvisorData.get("judgeTitleAndName")).isEqualTo("District Judge last name");
        assertThat(judgeAndLegalAdvisorData.get("legalAdvisorName")).isEqualTo("legal");
    }

    @Test
    void shouldReturnAMapWithEmptyPlaceholdersWhenObjectIsNull() {
        final Map<String, Object> judgeAndLegalAdvisorData = commonCaseDataExtractionService
            .getJudgeAndLegalAdvisorData(null);

        assertThat(judgeAndLegalAdvisorData.get("judgeTitleAndName")).isEqualTo(EMPTY_PLACEHOLDER);
        assertThat(judgeAndLegalAdvisorData.get("legalAdvisorName")).isEqualTo(EMPTY_PLACEHOLDER);
    }


    private HearingBooking createHearingBookingWithTimesOnSameDay() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 11, 16, 30));
    }

    private HearingBooking createHearingBookingWithTimesOnDifferentDays() {
        return createHearingBooking(LocalDateTime.of(2020, 12, 11, 15, 30),
            LocalDateTime.of(2020, 12, 12, 16, 30));
    }
}
