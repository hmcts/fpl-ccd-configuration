package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
public class JudicialMessageMetaData {
    private final String sender;
    private final String recipient;
    private final String urgency;
}
