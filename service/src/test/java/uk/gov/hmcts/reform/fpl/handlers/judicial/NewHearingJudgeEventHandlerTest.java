package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.NewHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewHearingJudgeEventHandlerTest {

    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private NewHearingJudgeEventHandler underTest;

    @Test
    void shouldNotDoAnythingIfNoHearing() {
        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder().build();

        underTest.handleNewHearingJudge(event);

        verifyNoInteractions(judicialService);
    }

    @Test
    void shouldNotDoAnythingIfNoHearingJudge() {
        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).build())
            .hearing(HearingBooking.builder()
                .build())
            .build();

        underTest.handleNewHearingJudge(event);

        verifyNoInteractions(judicialService);
    }

    @Test
    void shouldNotDoAnythingIfNoHearingJudgeJudicialUser() {
        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).build())
            .hearing(HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Test")
                    .build())
                .build())
            .build();

        underTest.handleNewHearingJudge(event);

        verifyNoInteractions(judicialService);
    }

    @Test
    void shouldAttemptAssignIfHearingJudgeJudicialUserWithIdamId() {
        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).build())
            .hearing(HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Test")
                    .judgeJudicialUser(JudicialUser.builder()
                        .idamId("1234")
                        .build())
                    .build())
                .build())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).assignHearingJudge(any(), eq("1234"), any(), any(), anyBoolean());
    }

    @Test
    void shouldAttemptAssignIfHearingJudgeJudicialUserWithPersonalCodeOnly() {
        when(judicialService.getJudge("personal"))
            .thenReturn(Optional.of(JudicialUserProfile.builder()
                    .sidamId("sidam")
                .build()));
        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).build())
            .hearing(HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                    .judgeLastName("Test")
                    .judgeJudicialUser(JudicialUser.builder()
                        .personalCode("personal")
                        .build())
                    .build())
                .build())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).getJudge("personal");
        verify(judicialService).assignHearingJudge(any(), eq("sidam"), any(), any(), anyBoolean());
    }

}
