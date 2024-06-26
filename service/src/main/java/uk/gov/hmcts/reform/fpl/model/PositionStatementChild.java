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
public class PositionStatementChild extends HearingDocument {
    private final UUID hearingId;
    private final String childName;
    private final UUID childId;
}
