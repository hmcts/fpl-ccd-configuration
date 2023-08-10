package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewAllocatedJudgeEventHandlerTest {

    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private NewAllocatedJudgeEventHandler underTest;

    @Test
    void shouldNotDoAnythingIfNoAllocatedJudge() {
        NewAllocatedJudgeEvent event = NewAllocatedJudgeEvent.builder().build();

        underTest.handleNewAllocatedJudge(event);

        verifyNoInteractions(judicialService);
    }

    @Test
    void shouldNotDoAnythingIfNoAllocatedJudgeJudicialUser() {
        NewAllocatedJudgeEvent event = NewAllocatedJudgeEvent.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .build())
            .build();

        underTest.handleNewAllocatedJudge(event);

        verifyNoInteractions(judicialService);
    }

    @Test
    void shouldAttemptAssignIfAllocatedJudgeJudicialUserWithIdamId() {
        NewAllocatedJudgeEvent event = NewAllocatedJudgeEvent.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("sidam")
                    .build())
                .build())
            .build();

        underTest.handleNewAllocatedJudge(event);

        verify(judicialService).assignAllocatedJudge(any(), eq("sidam"), anyBoolean());
    }

    @Test
    void shouldAttemptAssignIfAllocatedJudgeJudicialUserWithPersonalCodeOnly() {
        when(judicialService.getJudge("personal"))
            .thenReturn(Optional.of(JudicialUserProfile.builder()
                    .sidamId("sidam")
                .build()));
        NewAllocatedJudgeEvent event = NewAllocatedJudgeEvent.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .personalCode("personal")
                    .build())
                .build())
            .build();

        underTest.handleNewAllocatedJudge(event);

        verify(judicialService).getJudge("personal");
        verify(judicialService).assignAllocatedJudge(any(), eq("sidam"), anyBoolean());
    }

}
