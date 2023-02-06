package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.OTHER_EXTENSION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseExtensionService.class})
class CaseExtensionServiceTest {

    private static final LocalDate DATE_SUBMITTED = LocalDate.of(2020, 1, 1);

    private static final LocalDate OTHER_DATE = LocalDate.of(2020, 3, 3);
    private static final LocalDate EXTENDED_OTHER = OTHER_DATE.plusWeeks(8);
    @Autowired
    private CaseExtensionService service;

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWithOtherExtensionDate() {
        CaseData data = CaseData.builder()
            .extensionDateOther(OTHER_DATE)
            .caseExtensionTimeList(OTHER_EXTENSION).build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate).isEqualTo(OTHER_DATE);
    }

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {
        CaseData data = CaseData.builder()
            .caseExtensionTimeList(EIGHT_WEEK_EXTENSION)
            .caseExtensionTimeConfirmationList(OTHER_EXTENSION)
            .eightWeeksExtensionDateOther(EXTENDED_OTHER)
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate).isEqualTo(EXTENDED_OTHER);
    }

    @Test
    void shouldGetCaseCompletionDateSubmittingWith8WeekExtension() {
        CaseData data = CaseData.builder()
            .caseExtensionTimeList(EIGHT_WEEK_EXTENSION)
            .caseExtensionTimeConfirmationList(EIGHT_WEEK_EXTENSION)
            .dateSubmitted(DATE_SUBMITTED)
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate).isEqualTo(DATE_SUBMITTED.plusWeeks(34));
    }

    @Test
    void shouldGetCaseCompletedByDateWhenNoCompletionDate() {
        CaseData data = CaseData.builder().dateSubmitted(DATE_SUBMITTED).build();

        LocalDate caseCompletionDate = service.getCaseShouldBeCompletedByDate(data);

        assertThat(caseCompletionDate).isEqualTo(DATE_SUBMITTED.plusWeeks(26));
    }

    @Test
    void shouldGetCaseCompletedByDateWhenCompletionDateExists() {
        CaseData data = CaseData.builder()
            .dateSubmitted(DATE_SUBMITTED)
            .caseCompletionDate(OTHER_DATE)
            .build();

        LocalDate caseCompletionDate = service.getCaseShouldBeCompletedByDate(data);

        assertThat(caseCompletionDate).isEqualTo(OTHER_DATE);
    }
}
