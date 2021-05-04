package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class AuditEventsResponse {
    private final List<AuditEvent> auditEvents;
}
