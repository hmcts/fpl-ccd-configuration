package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.judicial.SyncHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.service.HearingJudgeService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SyncHearingJudgeEventHandler {

    private final HearingJudgeService hearingJudgeService;

    @Async
    @EventListener
    public void handleSyncHearingJudge(final SyncHearingJudgeEvent event) {
        hearingJudgeService.syncHearingJudgeRoles(event.getCaseData().getId(), event.getCaseData().getAllHearings());
    }

}
