package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class CaseSummary extends HearingDocument {

}
