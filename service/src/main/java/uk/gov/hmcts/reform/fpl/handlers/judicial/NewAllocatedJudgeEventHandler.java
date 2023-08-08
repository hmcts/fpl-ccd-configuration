package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.NewAllocatedJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewAllocatedJudgeEventHandler {

    private final JudicialService judicialService;

    @Async
    @EventListener
    public void handleNewAllocatedJudge(final NewAllocatedJudgeEvent event) {
        Judge allocatedJudge = event.getAllocatedJudge();
        if (isEmpty(allocatedJudge)) {
            // no allocated judge, return
            log.error("No allocated judge to attempt role assignment on");
            return;
        }

        if (!isEmpty(allocatedJudge.getJudgeJudicialUser())
            && !isEmpty(allocatedJudge.getJudgeJudicialUser().getIdamId())) {
            // have an IDAM ID - use that to grant the role
            judicialService.assignAllocatedJudge(event.getCaseId(),
                allocatedJudge.getJudgeJudicialUser().getIdamId(),
                allocatedJudge.getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR));
        } else if (!isEmpty(allocatedJudge.getJudgeJudicialUser())
            && !isEmpty(allocatedJudge.getJudgeJudicialUser().getPersonalCode())) {
            // no IDAM ID, but has personal code, lookup in JRD first
            Optional<JudicialUserProfile> judge = judicialService
                .getJudge(allocatedJudge.getJudgeJudicialUser().getPersonalCode());

            judge.ifPresentOrElse(judicialUserProfile ->
                    judicialService.assignAllocatedJudge(event.getCaseId(), judicialUserProfile.getSidamId(),
                        allocatedJudge.getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR)),
                () -> log.info("Could not lookup in JRD, no auto allocation of judge on case {}", event.getCaseId()));
        } else {
            log.info("No auto allocation of judge on case {}", event.getCaseId());
        }
    }
}
