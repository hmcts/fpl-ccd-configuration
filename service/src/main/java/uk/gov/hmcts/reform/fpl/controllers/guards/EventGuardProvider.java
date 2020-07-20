package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

@Service
public class EventGuardProvider {

    @Autowired
    CaseSubmissionValidator caseSubmissionGuard;

    @Autowired
    PassThroughGuard passThroughGuard;

    EnumMap<FplEvent, EventValidator> guards = new EnumMap<>(FplEvent.class);

    @PostConstruct
    public void init() {
        guards.put(FplEvent.SUBMIT_APPLICATION, caseSubmissionGuard);
    }

    public EventValidator getEventGuard(FplEvent event) {
        return guards.getOrDefault(event, passThroughGuard);
    }
}
