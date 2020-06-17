package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.FplEvent;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

@Service
public class EventGuardProvider {

    @Autowired
    GeneratedOrderGuard generatedOrderGuard;

    @Autowired
    HearingDetailsGuard hearingDetailsGuard;

    @Autowired
    NotifyGatekeeperGuard notifyGatekeeperGuard;

    @Autowired
    PassThroughGuard passThroughGuard;

    EnumMap<FplEvent, EventGuard> guards = new EnumMap<>(FplEvent.class);

    @PostConstruct
    public void init() {
        guards.put(FplEvent.CREATE_ORDER, generatedOrderGuard);
        guards.put(FplEvent.HEARING_DETAILS, hearingDetailsGuard);
        guards.put(FplEvent.NOTIFY_GATEKEEPER, notifyGatekeeperGuard);
    }

    public EventGuard getEventGuard(FplEvent event) {
        return guards.getOrDefault(event, passThroughGuard);
    }
}
