package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;

@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
public class RecordFinalDecisionsControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private JudicialService judicialService;

    RecordFinalDecisionsControllerSubmittedTest() {
        super("record-final-decisions");
    }

    @Test
    void shouldCleanupRolesWhenCaseClosed() {
        postSubmittedEvent(toCallBackRequest(CaseData.builder().id(1L).state(CLOSED).build(),
            CaseData.builder().id(1L).state(CASE_MANAGEMENT).build()));

        verify(judicialService).deleteAllRolesOnCase(any());
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldNotCleanupRolesWhenCaseNotClosed(State state) {
        postSubmittedEvent(toCallBackRequest(CaseData.builder().id(1L).state(state).build(),
            CaseData.builder().id(1L).state(state).build()));

        verify(judicialService, never()).deleteAllRolesOnCase(any());
    }

}
