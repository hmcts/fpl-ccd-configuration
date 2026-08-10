package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TemporaryApplicationDocuments", generate = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationDocument implements FurtherDocument, WithDocument {
    @CCD(label = "Allow marking document confidential", typeOverride = FieldType.YesOrNo)
    private String allowMarkDocumentConfidential;
    @CCD(label = "File", typeOverride = FieldType.Document)
    private final DocumentReference document;
    @CCD(
            label = "Document Uploader Type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentUploaderType"
    )
    private DocumentUploaderType uploaderType;
    @CCD(label = "Document Uploader Case Roles")
    private List<CaseRole> uploaderCaseRoles;
    @CCD(label = "Document type", typeOverride = FieldType.FixedList, typeParameterOverride = "ApplicationDocumentType")
    private final ApplicationDocumentType documentType;
    @CCD(label = "Date and time uploaded")
    protected LocalDateTime dateTimeUploaded;
    @CCD(label = "Uploaded by")
    private String uploadedBy;
    @CCD(
            label = "Document name",
            hint = "Use a meaningful name. For example, medical report",
            showCondition = "documentType=\"OTHER\"",
            regex = "^(?!.*<[^>\\d]+>*).*"
    )
    private String documentName;
    @CCD(
            label = "Included in SWET",
            hint = "Use a meaningful name. For example, Genogram",
            showCondition = "documentType=\"SWET\"",
            typeOverride = FieldType.TextArea
    )
    private String includedInSWET;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge",
            typeParameterClass = DocumentAcknowledge.class
    )
    private List<String> documentAcknowledge;
    @CCD(
            label = "Tick to restrict to the LA, Cafcass and HMCTS staff",
            showCondition = "confidentialWarningLabel=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfidentialDocument",
            typeParameterClass = ConfidentialDocument.class
    )
    private List<String> confidential;
    @CCD(label = "Reason for removal")
    private String removalReason;
    @CCD(label = "Is confidential?", typeOverride = FieldType.YesOrNo)
    private String markAsConfidential;
    @CCD(label = "Is translation needed?")
    private final LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    public boolean hasDocument() {
        return document != null;
    }

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return confidential != null && confidential.contains("CONFIDENTIAL");
    }

    @JsonIgnore
    public String getName() {
        return documentType.getLabel();
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
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String confidentialWarningLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  // ==== end synthesised definition-only fields ====
}
