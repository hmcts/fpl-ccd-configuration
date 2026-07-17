package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementNoticeDocument implements WithDocument {

    @CCD(
            label = "Recipient type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PlacementNoticeRecipientType"
    )
    private RecipientType type;
    @CCD(
            label = "Notice of placement response",
            categoryID = "placementApplicationsAndResponses",
            typeOverride = FieldType.Document
    )
    private DocumentReference response;
    @CCD(label = "Notice of placement response description", typeOverride = FieldType.TextArea)
    private String responseDescription;
    @CCD(label = "Party")
    private String recipientName;
    @CCD(label = "Respondent id", showCondition = "response=\"DO NOT SHOW\"", typeOverride = FieldType.Text)
    private UUID respondentId;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge"
    )
    private List<String> documentAcknowledge;
    @CCD(
            label = "Document Uploader Type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentUploaderType"
    )
    private DocumentUploaderType uploaderType;
    @CCD(label = "Document Uploader Case Roles")
    private List<CaseRole> uploaderCaseRoles;
    @CCD(label = "Reason for removal", typeOverride = FieldType.TextArea)
    private String removalReason;
    @CCD(ignore = true)
    private LanguageTranslationRequirement translationRequirements;

    @JsonIgnore
    public DocumentReference getDocument() {
        return response;
    }

    public String getMarkAsConfidential() {
        return null;
    }

    @Getter
    @RequiredArgsConstructor
    public enum RecipientType {
        LOCAL_AUTHORITY("Local authority"),
        CAFCASS("Cafcass"),
        PARENT_FIRST("First parent"),
        PARENT_SECOND("Second parent"),
        RESPONDENT("Respondent");

        private final String name;

        public static final List<RecipientType> PARENT_TYPES = asList(PARENT_FIRST, PARENT_SECOND);
    }

    public List<String> getDocumentAcknowledge() {
        if (this.documentAcknowledge == null) {
            this.documentAcknowledge = new ArrayList<>();
        }
        if (response != null && !this.documentAcknowledge.contains(DOCUMENT_ACKNOWLEDGEMENT_KEY)) {
            this.documentAcknowledge.add(DOCUMENT_ACKNOWLEDGEMENT_KEY);
        }
        return this.documentAcknowledge;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  // ==== end synthesised definition-only fields ====
}
