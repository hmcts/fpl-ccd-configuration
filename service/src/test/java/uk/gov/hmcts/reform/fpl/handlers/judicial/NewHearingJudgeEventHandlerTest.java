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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    void shouldAttemptAssignIfHearingJudgeJudicialUserWithIdamId() {
        HearingBooking booking = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).hearingDetails(wrapElements(booking)).build())
            .hearing(booking)
            .oldHearing(Optional.empty())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).assignHearingJudge(12345L, booking, Optional.empty(), true);
    }

    @Test
    void shouldAttemptToAssignRoleWithCorrectFollowUpHearing() {
        HearingBooking booking = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        HearingBooking existingBookingAfter = booking.toBuilder()
            .startDate(LocalDateTime.now().plusDays(2))
            .build();

        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L)
                .hearingDetails(wrapElements(booking, existingBookingAfter)).build())
            .hearing(booking)
            .oldHearing(Optional.empty())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).assignHearingJudge(12345L, booking, Optional.of(existingBookingAfter), false);
    }


    @Test
    void shouldAttemptAssignIfHearingJudgeJudicialUserWithPersonalCodeOnly() {
        HearingBooking booking = HearingBooking.builder()
            .startDate(LocalDateTime.now())
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .personalCode("personal")
                    .build())
                .build())
            .build();

        NewHearingJudgeEvent event = NewHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).hearingDetails(wrapElements(booking)).build())
            .hearing(booking)
            .oldHearing(Optional.empty())
            .build();

        underTest.handleNewHearingJudge(event);

        verify(judicialService).assignHearingJudge(12345L, event.getHearing(), Optional.empty(), true);
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
        verify(judicialService).assignHearingJudge(12345L, newHearing, Optional.empty(), false);
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
        verify(judicialService).assignHearingJudge(12345L, newHearing, Optional.empty(), false);
        verifyNoMoreInteractions(judicialService);
    }

}
