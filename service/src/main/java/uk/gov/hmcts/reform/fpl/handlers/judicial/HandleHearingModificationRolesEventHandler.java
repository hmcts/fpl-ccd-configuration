package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.judicial.HandleHearingModificationRolesEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
 import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HandleHearingModificationRolesEventHandler {

    private final JudicialService judicialService;

    @EventListener
    public void handleCancelledHearingRoles(final HandleHearingModificationRolesEvent event) {
        // Not an async function as it has to take place before we grant more roles, in case the times overlap
        // when relisting. it has to be caught though to make sure nothing else afterward is impacted in case of
        // failure
        final Long caseId = event.getCaseData().getId();

        try {
            nullSafeList(event.getCaseData().getCancelledHearingDetails())
                .forEach(hearing -> {
                    if (findElement(hearing.getId(), event.getCaseDataBefore().getCancelledHearingDetails())
                        .isEmpty()) {
                        // new cancelled hearing - need to attempt deletion
                        judicialService.deleteSpecificHearingRole(caseId, hearing.getValue());

                        // find the latest active hearing before this one and fix its roles
                        Optional<HearingBooking> lastHearing = event.getCaseData()
                            .getLastHearingBefore(hearing.getValue().getStartDate());

                        if (lastHearing.isPresent()) {
                            judicialService.deleteSpecificHearingRole(caseId, lastHearing.get());

                            Optional<HearingBooking> possibleNextHearing = event.getCaseData()
                                .getNextHearingAfter(lastHearing.get().getStartDate());

                            judicialService.assignHearingJudge(caseId, lastHearing.get(), possibleNextHearing);
                        }
                    }
                });
        } catch (Exception e) {
            log.error("Error when handling roles on cancelled hearings", e);
        }
    }
}
