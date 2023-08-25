package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class PositionStatementRespondent extends HearingDocument {
    private final String respondentName;
    private final UUID respondentId;
    private final UUID hearingId;
}
