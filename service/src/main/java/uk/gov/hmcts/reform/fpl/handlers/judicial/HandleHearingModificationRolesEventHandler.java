package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.judicial.HandleHearingModificationRolesEvent;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

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

        try {
            event.getCaseData().getCancelledHearingDetails()
                .forEach(hearing -> {
                    if (findElement(hearing.getId(), event.getCaseDataBefore().getCancelledHearingDetails())
                        .isEmpty()) {
                        // new cancelled hearing - need to attempt deletion
                        judicialService.deleteSpecificHearingRole(event.getCaseData().getId(), hearing.getValue());
                    }
                });
        } catch (Exception e) {
            log.error("Error when handling roles on cancelled hearings", e);
        }
    }
}
