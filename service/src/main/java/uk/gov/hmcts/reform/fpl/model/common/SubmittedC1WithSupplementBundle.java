package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;
import uk.gov.hmcts.reform.fpl.model.YesOrNoFixedList;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SubmittedC1WithSupplement", generate = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SubmittedC1WithSupplementBundle  {
    @CCD(
            label = "File",
            regex = ".doc,.docx,.pdf",
            categoryID = "originalApplications",
            searchable = false,
            typeOverride = FieldType.Document
    )
    private final DocumentReference document;
    @CCD(label = "Please state how soon you want the judge to consider your application?", searchable = false)
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @CCD(
            label = "Supporting documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1SupportingEvidenceBundleOriginal"
    )
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @CCD(
            label = "Supplements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1SupplementOriginal"
    )
    private final List<Element<Supplement>> supplementsBundle;
    @CCD(
            label = "Remove this C1 with supplement application.",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "YesOrNoFixedList",
            typeParameterClass = YesOrNoFixedList.class
    )
    private final String clearSubmittedC1WithSupplement;
    @CCD(
            label = "Is Document Uploaded?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "YesOrNoFixedList",
            typeParameterClass = YesOrNoFixedList.class
    )
    private final String isDocumentUploaded;

    public String getClearSubmittedC1WithSupplement() {
        if (clearSubmittedC1WithSupplement != null) {
            return this.clearSubmittedC1WithSupplement;
        }
        return document != null ? YesNo.NO.getValue().toUpperCase() : null;
    }

    public String getIsDocumentUploaded() {
        return YesNo.from(document != null).getValue().toUpperCase();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
  private java.util.Set<DocumentAcknowledge> documentAcknowledge;
  // ==== end synthesised definition-only fields ====
}
