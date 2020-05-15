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
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseExtensionService.class})
class CaseExtensionServiceTest {

    @Autowired
    private CaseExtensionService service;

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWithOtherExtensionDate() {
        LocalDate extensionDateOther = LocalDate.of(2030, 11, 12);

        CaseData data = CaseData.builder()
            .extensionDateOther(extensionDateOther)
            .caseExtensionTimeList(otherExtension).build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate.isEqual(extensionDateOther));
    }

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {
        LocalDate eightWeeksExtensionDateOther = LocalDate.of(2030, 11, 12);

        CaseData data = CaseData.builder()
            .caseExtensionTimeList(eightWeekExtension)
            .caseExtensionTimeConfirmationList(otherExtension)
            .eightWeeksExtensionDateOther(eightWeeksExtensionDateOther)
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate.isEqual(eightWeeksExtensionDateOther));
    }

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWhenSubmittingWith8WeekExtension() {
        LocalDate dateSubmitted = LocalDate.of(2030,11,11);

        CaseData data = CaseData.builder()
            .caseExtensionTimeList(eightWeekExtension)
            .caseExtensionTimeConfirmationList(eightWeekExtension)
            .dateSubmitted(dateSubmitted)
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(data);

        assertThat(caseCompletionDate.isEqual(dateSubmitted.plusWeeks(8)));
    }

    @Test
    void shouldGetCaseCompletionDateFor8WeekExtension() {
        CaseData data = CaseData.builder().dateSubmitted(LocalDate
            .of(2020, 11, 20)).build();

        LocalDate expectedCaseCompletionDate = data.getDateSubmitted().plusWeeks(8);

        LocalDate caseCompletionDate = service.getCaseCompletionDateFor8WeekExtension(data);

        assertThat(caseCompletionDate.isEqual(expectedCaseCompletionDate));
    }

    @Test
    void shouldGetCaseSubmittedDateWhenNoCompletionDate() {
        CaseData data = CaseData.builder().dateSubmitted(LocalDate
            .of(2020, 11, 20))
            .caseCompletionDate(LocalDate.of(2030, 11, 20)).build();

        LocalDate expectedCaseCompletionDate = data.getCaseCompletionDate();

        LocalDate caseCompletionDate = service.getCaseCompletionOrSubmittedDate(data);

        assertThat(caseCompletionDate.isEqual(expectedCaseCompletionDate));
    }

    @Test
    void shouldGetCaseCompletionDateWhenCompletionDateExists() {
        CaseData data = CaseData.builder().dateSubmitted(LocalDate
            .of(2020, 11, 20)).build();

        LocalDate expectedCaseCompletionDate = data.getDateSubmitted().plusWeeks(8);

        LocalDate caseCompletionDate = service.getCaseCompletionOrSubmittedDate(data);

        assertThat(caseCompletionDate.isEqual(expectedCaseCompletionDate));
    }
}
