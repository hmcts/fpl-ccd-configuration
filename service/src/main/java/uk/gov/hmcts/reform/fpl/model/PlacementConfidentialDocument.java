package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementConfidentialDocument {
    @CCD(
            label = "Document type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PlacementConfidentialDocumentType",
            typeParameterClass = PlacementConfidentialDocumentType.class
    )
    private Type type;
    @CCD(label = "Custom document type")
    private String otherDocTypeName;
    @CCD(
            label = "Document",
            categoryID = "placementApplicationsAndResponsesConfidential",
            typeOverride = FieldType.Document
    )
    private DocumentReference document;
    @CCD(label = "Description", typeOverride = FieldType.TextArea)
    private String description;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge",
            typeParameterClass = DocumentAcknowledge.class
    )
    private List<String> documentAcknowledge;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        ANNEX_B("Annex B"),
        GUARDIANS_REPORT("Guardian's report"),
        OTHER_CONFIDENTIAL_DOCUMENTS("Other confidential documents (please specify below)");

        private final String name;
    }

    public List<String> getDocumentAcknowledge() {
        if (this.documentAcknowledge == null) {
            this.documentAcknowledge = new ArrayList<>();
        }
        if (document != null && !this.documentAcknowledge.contains(DOCUMENT_ACKNOWLEDGEMENT_KEY)) {
            this.documentAcknowledge.add(DOCUMENT_ACKNOWLEDGEMENT_KEY);
        }
        return this.documentAcknowledge;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "<div class='govuk-tag govuk-tag--red'>Confidential</div>", typeOverride = FieldType.Label)
  private String tag;
  // ==== end synthesised definition-only fields ====
}
