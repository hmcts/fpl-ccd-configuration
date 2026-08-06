package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;

import java.util.UUID;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class RespondentStatementV2 extends SupportingEvidenceBundle implements NotifyDocumentUploaded {
    @CCD(label = "Respondent", searchable = false)
    private String respondentName;
    @CCD(
            label = "Id of the respondent for this respondent statement",
            showCondition = "respondentName=\"DO NOT SHOW\"",
            searchable = false,
            typeOverride = FieldType.Text
    )
    private UUID respondentId;

    @Override
    public String asLabel() {
        return String.format("%s - %s", "Respondent Statement", getDocument().getFilename());
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String confidentialWarningLabel;
  @CCD(label = " ", showCondition = "confidential CONTAINS \"CONFIDENTIAL\"", searchable = false)
  private String confidentialTabLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String hasConfidentialAddressLabel;
  // ==== end synthesised definition-only fields ====
}
