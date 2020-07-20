package uk.gov.hmcts.reform.fpl;

import lombok.Data;

@Data
public class EventStatus {
    private final FplEvent event;
    private final EventState state;
}
