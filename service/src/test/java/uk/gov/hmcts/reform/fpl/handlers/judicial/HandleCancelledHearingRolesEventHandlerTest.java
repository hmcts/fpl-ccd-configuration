package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.judicial.HandleCancelledHearingRolesEvent;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class HandleCancelledHearingRolesEventHandlerTest {

    @Mock
    private JudicialService judicialService;

    @InjectMocks
    private HandleCancelledHearingRolesEventHandler underTest;

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

        underTest.handleCancelledHearingRoles(new HandleCancelledHearingRolesEvent(
            CaseData.builder().id(12345L).cancelledHearingDetails(newCancelled).build(),
            CaseData.builder().id(12345L).cancelledHearingDetails(oldCancelled).build()
        ));

        verify(judicialService).deleteSpecificHearingRole(12345L, newCancelledBooking);
    }
}
