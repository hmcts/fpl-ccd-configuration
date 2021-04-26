package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.PreSubmissionTask;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PreSubmissionTasksServiceTest {
    @Mock
    private EventsChecker eventsChecker;

    @InjectMocks
    private PreSubmissionTasksService preSubmissionTasksService;

    @Test
    void shouldAddValidationMessages() {
        CaseData caseData = CaseData.builder().build();

        final String changeCaseNameMessage = "Change case name";
        when(eventsChecker.validate(Event.CASE_NAME, caseData)).thenReturn(List.of(changeCaseNameMessage));

        List<PreSubmissionTask> eventValidationsForSubmission =
            preSubmissionTasksService.getPreSubmissionTasks(caseData);

        assertThat(eventValidationsForSubmission.contains(PreSubmissionTask.builder()
            .event(Event.CASE_NAME)
            .messages(List.of(changeCaseNameMessage))
            .build()));
    }
}
