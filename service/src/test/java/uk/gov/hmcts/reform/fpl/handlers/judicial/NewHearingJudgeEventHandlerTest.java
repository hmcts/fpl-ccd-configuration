package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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
            .oldHearing(Optional.empty())
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
            .oldHearing(Optional.empty())
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
            .oldHearing(Optional.empty())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).getJudge("personal");
        verify(judicialService).assignHearingJudge(any(), eq("sidam"), any(), any(), anyBoolean());
    }

    @Test
    void shouldHandleEditedHearingRolesWhenTimeChanges() {
        LocalDateTime now = LocalDateTime.now();
        UUID hearingId = UUID.randomUUID();

        HearingBooking oldHearing = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId")
                    .build())
                .build())
            .build();

        HearingBooking newHearing = oldHearing.toBuilder()
            .startDate(now.plusDays(2))
            .build();

        underTest.handleNewHearingJudge(new NewHearingJudgeEvent(
            newHearing,
            CaseData.builder().id(12345L).hearingDetails(List.of(element(hearingId, newHearing))).build(),
            Optional.of(oldHearing)
        ));

        verify(judicialService).deleteSpecificHearingRole(12345L, oldHearing);
        verify(judicialService).assignHearingJudge(any(), eq("idamId"), any(), any(), anyBoolean());
        verifyNoMoreInteractions(judicialService);
    }

    @Test
    void shouldHandleEditedHearingRolesWhenJudgeChanges() {
        LocalDateTime now = LocalDateTime.now();
        UUID hearingId = UUID.randomUUID();

        HearingBooking oldHearing = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId")
                    .build())
                .build())
            .build();

        HearingBooking newHearing = oldHearing.toBuilder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId2")
                    .build())
                .build())
            .build();

        underTest.handleNewHearingJudge(new NewHearingJudgeEvent(
            newHearing,
            CaseData.builder().id(12345L).hearingDetails(List.of(element(hearingId, newHearing))).build(),
            Optional.of(oldHearing)
        ));

        verify(judicialService).deleteSpecificHearingRole(12345L, oldHearing);
        verify(judicialService).assignHearingJudge(any(), eq("idamId2"), any(), any(), anyBoolean());
        verifyNoMoreInteractions(judicialService);
    }

    @Test
    void shouldCreateHearingRoleInFutureIfMultipleHearings() {
        LocalDateTime future = LocalDateTime.now();

        HearingBooking hearing1 = HearingBooking.builder()
            .startDate(future)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId")
                    .build())
                .build())
            .build();

        HearingBooking hearing2 = hearing1.toBuilder()
            .startDate(future.plusDays(2))
            .build();

        underTest.handleNewHearingJudge(new NewHearingJudgeEvent(
            hearing2,
            CaseData.builder().id(12345L).hearingDetails(wrapElements(hearing1, hearing2)).build(),
            Optional.empty()
        ));

        verify(judicialService).assignHearingJudge(12345L,
            "idamId",
            future.plusDays(2).atZone(ZoneId.systemDefault()),
            null,
            false);
    }

    @Test
    void shouldCreateHearingRoleNowIfSingleHearing() {
        final LocalDateTime now = LocalDateTime.now();
        final ZonedDateTime nowZoned = now.atZone(ZoneId.systemDefault());

        HearingBooking hearing1 = HearingBooking.builder()
            .startDate(now.plusDays(2))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId")
                    .build())
                .build())
            .build();

        try (MockedStatic<ZonedDateTime> zonedStatic = mockStatic(ZonedDateTime.class)) {
            zonedStatic.when(ZonedDateTime::now).thenReturn(nowZoned);

            underTest.handleNewHearingJudge(new NewHearingJudgeEvent(
                hearing1,
                CaseData.builder().id(12345L).hearingDetails(wrapElements(hearing1)).build(),
                Optional.empty()
            ));

            verify(judicialService).assignHearingJudge(12345L,
                "idamId",
                nowZoned,
                null,
                false);
        }
    }
}
