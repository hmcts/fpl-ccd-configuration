package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class SkeletonArgument extends HearingDocument {
    @CCD(label = "Party name")
    private final String partyName;
    @CCD(label = "Party", showCondition = "partyId=\"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private final UUID partyId;
    @CCD(label = "Hearing Id", showCondition = "hearingId=\"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private final UUID hearingId;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String hasConfidentialAddressLabel;
  // ==== end synthesised definition-only fields ====
}
