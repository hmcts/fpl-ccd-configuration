package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.HandleHearingModificationRolesEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class HandleHearingModificationRolesEventHandlerTest {

    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private HandleHearingModificationRolesEventHandler underTest;

    @Test
    void shouldHandleCancelledHearingRoles() {
        LocalDateTime now = LocalDateTime.now();

        HearingBooking newCancelledBooking = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("idamId")
                    .build())
                .build())
            .build();

        List<Element<HearingBooking>> oldCancelled = List.of();
        List<Element<HearingBooking>> newCancelled = List.of(element(newCancelledBooking));

        underTest.handleCancelledHearingRoles(new HandleHearingModificationRolesEvent(
            CaseData.builder().id(12345L).cancelledHearingDetails(newCancelled).build(),
            CaseData.builder().id(12345L).cancelledHearingDetails(oldCancelled).build()
        ));

        verify(judicialService).deleteSpecificHearingRole(12345L, newCancelledBooking);
        verifyNoMoreInteractions(judicialService);
    }

    @Test
    void shouldReconfigurePreviousHearingRoleWhenNoFurtherHearings() {
        LocalDateTime now = LocalDateTime.now();
        UUID cancelledBookingId = UUID.randomUUID();

        HearingBooking prevBooking = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        HearingBooking cancelledBooking = HearingBooking.builder()
            .startDate(now.plusDays(2))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();


        CaseData caseDataBefore = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(element(prevBooking), element(cancelledBookingId, cancelledBooking)))
            .build();

        CaseData caseDataAfter = caseDataBefore.toBuilder()
            .hearingDetails(List.of(element(prevBooking)))
            .cancelledHearingDetails(List.of(element(cancelledBookingId, cancelledBooking)))
            .build();

        underTest.handleCancelledHearingRoles(new HandleHearingModificationRolesEvent(caseDataAfter, caseDataBefore));

        verify(judicialService).deleteSpecificHearingRole(12345L, cancelledBooking);
        verify(judicialService).deleteSpecificHearingRole(12345L, prevBooking);
        verify(judicialService).assignHearingJudge(12345L, prevBooking, Optional.empty());
    }

    @Test
    void shouldReconfigurePreviousHearingRoleWhenFurtherHearings() {
        LocalDateTime now = LocalDateTime.now();
        UUID cancelledBookingId = UUID.randomUUID();

        HearingBooking prevBooking = HearingBooking.builder()
            .startDate(now)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        HearingBooking cancelledBooking = HearingBooking.builder()
            .startDate(now.plusDays(2))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        HearingBooking futureHearing = HearingBooking.builder()
            .startDate(now.plusDays(4))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
                .judgeLastName("Test")
                .judgeJudicialUser(JudicialUser.builder()
                    .idamId("1234")
                    .build())
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .id(12345L)
            .hearingDetails(List.of(
                element(prevBooking),
                element(cancelledBookingId, cancelledBooking),
                element(futureHearing)))
            .build();

        CaseData caseDataAfter = caseDataBefore.toBuilder()
            .hearingDetails(wrapElements(prevBooking, futureHearing))
            .cancelledHearingDetails(List.of(element(cancelledBookingId, cancelledBooking)))
            .build();

        underTest.handleCancelledHearingRoles(new HandleHearingModificationRolesEvent(caseDataAfter, caseDataBefore));

        verify(judicialService).deleteSpecificHearingRole(12345L, cancelledBooking);
        verify(judicialService).deleteSpecificHearingRole(12345L, prevBooking);
        verify(judicialService).assignHearingJudge(12345L, prevBooking, Optional.of(futureHearing));
    }

    @Test
    void shouldNotThrowExceptionIfNullList() {
        assertDoesNotThrow(() -> underTest.handleCancelledHearingRoles(new HandleHearingModificationRolesEvent(
            CaseData.builder().id(12345L).cancelledHearingDetails(null).build(),
            CaseData.builder().id(12345L).cancelledHearingDetails(null).build()
        )));
    }
}
