package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;

@ExtendWith(SpringExtension.class)
class PreSubmissionTasksRendererTest {
    @Spy
    PreSubmissionTasksRenderer preSubmissionTasksRenderer;

    @Test
    void shouldContainErrorMessageFromError() {
        final String caseNameMessage = "Create case name";
        List<EventValidation> validationErrors = List.of(
            EventValidation.builder().event(CASE_NAME).messages(List.of(caseNameMessage)).build()
        );

        assertThat(preSubmissionTasksRenderer.render(validationErrors).contains(caseNameMessage));
    }

    @Test
    void shouldRenderEmptyMessageWhenNoErrors() {
        assertThat(preSubmissionTasksRenderer.render(List.of()).isEmpty());
    }
}
