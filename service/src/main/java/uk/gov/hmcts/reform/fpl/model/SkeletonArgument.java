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
public class SkeletonArgument extends HearingDocument {
    private final String partyName;
    private final UUID partyId;
    private final UUID hearingId;
}
