package uk.gov.hmcts.reform.fpl.handlers.judicial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.SyncHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.JudicialUser;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.HearingJudgeService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class SyncHearingJudgeEventHandlerTest {

    @Mock
    private HearingJudgeService hearingJudgeService;

    @InjectMocks
    private SyncHearingJudgeEventHandler underTest;

    @Test
    void shouldPassThroughToHearingJudgeService() {
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

        List<Element<HearingBooking>> hearings = wrapElements(booking);

        SyncHearingJudgeEvent event = SyncHearingJudgeEvent.builder()
            .caseData(CaseData.builder().id(12345L).hearingDetails(hearings).build())
            .build();

        underTest.handleSyncHearingJudge(event);

        verify(hearingJudgeService).syncHearingJudgeRoles(12345L, hearings);
    }

}
