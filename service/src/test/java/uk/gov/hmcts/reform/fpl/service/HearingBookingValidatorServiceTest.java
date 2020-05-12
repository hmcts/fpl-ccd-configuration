package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.validation.groups.HearingBookingDetailsGroup;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HearingBookingValidatorService.class, ValidateGroupService.class,
    LocalValidatorFactoryBean.class})
class HearingBookingValidatorServiceTest {

    private static final String VALIDATION_ERROR = "Error 1";

    @MockBean
    private ValidateGroupService validateGroupService;

    @Autowired
    private HearingBookingValidatorService service;

    @Test
    void shouldReturnEmptyValidationErrorsWhenNoHearingBookings() {
        final List<HearingBooking> hearingBookings = emptyList();
        final List<String> validationErrors = service.validateHearingBookings(hearingBookings);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsWhenSingleHearingBookings() {
        final HearingBooking hearingBooking = HearingBooking.builder()
            .venue("Venue")
            .build();

        when(validateGroupService.validateGroup(hearingBooking, HearingBookingDetailsGroup.class))
            .thenReturn(of(VALIDATION_ERROR));

        final List<String> validationErrors = service.validateHearingBookings(of(hearingBooking));

        assertThat(validationErrors).containsExactly(VALIDATION_ERROR);
    }

    @Test
    void shouldReturnValidationErrorsWhenMultipleHearingBookings() {
        final HearingBooking hearingBooking1 = HearingBooking.builder()
            .venue("Venue A")
            .build();
        final HearingBooking hearingBooking2 = HearingBooking.builder()
            .venue("Venue B")
            .build();

        when(validateGroupService.validateGroup(hearingBooking1, HearingBookingDetailsGroup.class))
            .thenReturn(emptyList());
        when(validateGroupService.validateGroup(hearingBooking2, HearingBookingDetailsGroup.class))
            .thenReturn(List.of(VALIDATION_ERROR));

        final List<String> validationErrors = service.validateHearingBookings(of(hearingBooking1, hearingBooking2));

        assertThat(validationErrors).containsExactly(String.format("%s for hearing 2", VALIDATION_ERROR));
    }

    @Test
    void shouldReturnValidationErrorsWhenAJudgeIsNotAllocatedToTheCase() {
        final String errorMessage = "You need to enter the allocated judge.";
        final CaseData caseData = CaseData.builder().build();

        when(validateGroupService.validateGroup(caseData, HearingBookingDetailsGroup.class))
            .thenReturn(List.of(errorMessage));

        final List<String> validationErrors = service.validateHasAllocatedJudge(caseData);

        assertThat(validationErrors).containsExactly(errorMessage);
    }

    @Test
    void shouldReturnNOEmptyValidationErrorsWhenAJudgeHasBeenAllocatedToTheCase() {
        final CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Davidson")
                .build())
            .build();

        final List<String> validationErrors = service.validateHasAllocatedJudge(caseData);

        assertThat(validationErrors).isEmpty();
    }
}
