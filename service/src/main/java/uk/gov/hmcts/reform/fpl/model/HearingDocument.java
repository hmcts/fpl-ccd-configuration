package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonSubTypes({
    @JsonSubTypes.Type(value = CourtBundle.class),
    @JsonSubTypes.Type(value = CaseSummary.class),
    @JsonSubTypes.Type(value = PositionStatementRespondent.class),
    @JsonSubTypes.Type(value = PositionStatementChild.class),
    @JsonSubTypes.Type(value = SkeletonArgument.class)
})
@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class HearingDocument extends DocumentMetaData implements WithDocument, NotifyDocumentUploaded {

    @CCD(label = "Court bundle for")
    protected String hearing;
    @CCD(label = "Upload a file", typeOverride = FieldType.Document)
    protected DocumentReference document;
    @CCD(label = "Reason for removal", typeOverride = FieldType.TextArea)
    @Setter
    protected String removalReason;
    @CCD(label = "Document contains a confidential address?", searchable = false, typeOverride = FieldType.YesOrNo)
    protected String hasConfidentialAddress;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge"
    )
    protected List<String> documentAcknowledge;
    @CCD(
            label = "Document Uploader Type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentUploaderType"
    )
    protected DocumentUploaderType uploaderType;
    @CCD(label = "Document Uploader Case Roles")
    protected List<CaseRole> uploaderCaseRoles;
    @CCD(label = "Is confidential?", typeOverride = FieldType.YesOrNo)
    protected String markAsConfidential;
    @CCD(label = "Is translation needed?")
    protected LanguageTranslationRequirement translationRequirements;

    public String getHasConfidentialAddress() {
        return (document != null && (!YesNo.isYesOrNo(hasConfidentialAddress)))
            ? YesNo.NO.getValue() : hasConfidentialAddress;
    }

    @JsonIgnore
    @Override
    public DocumentReference getTypeOfDocument() {
        return document;
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
}
