package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.FurtherDocument;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.validation.interfaces.time.PastOrPresentDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonSubTypes({
    @JsonSubTypes.Type(value = RespondentStatementV2.class)
})
@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportingEvidenceBundle implements TranslatableItem, FurtherDocument, WithDocument {
    @CCD(label = "Document name", regex = "^(?!.*<[^>\\d]+>*).*")
    private final String name;
    @CCD(label = "Notes", typeOverride = FieldType.TextArea)
    private final String notes;
    @CCD(label = "Date and time received", hint = "For example, 31 3 2016  2 30 00")
    @PastOrPresentDate(message = "Date received cannot be in the future")
    private final LocalDateTime dateTimeReceived;
    @CCD(label = "Date and time uploaded")
    private LocalDateTime dateTimeUploaded;
    @CCD(label = "File", categoryID = "respondentsOwnStatements", typeOverride = FieldType.Document)
    private final DocumentReference document;
    @CCD(
            label = "Document Uploader Type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentUploaderType"
    )
    private DocumentUploaderType uploaderType;
    @CCD(label = "Document Uploader Case Roles")
    private List<CaseRole> uploaderCaseRoles;
    @CCD(label = "Uploaded by")
    private String uploadedBy;
    @CCD(
            label = " ",
            showCondition = "confidentialWarningLabel=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfidentialDocument",
            typeParameterClass = ConfidentialDocument.class
    )
    private List<String> confidential;
    @CCD(
            label = "Choose a further evidence document type",
            showCondition = "confidential=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private FurtherEvidenceType type;
    @CCD(
            label = " ",
            showCondition = "confidential=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private String uploadedBySolicitor;
    @CCD(label = "Translated document", categoryID = "parent_respondentsStatements", typeOverride = FieldType.Document)
    private final DocumentReference translatedDocument;
    @CCD(label = "Welsh translation upload time", showCondition = "translatedDocument=\"DO_NOT_SHOW\"")
    private final LocalDateTime translationUploadDateTime;
    @CCD(label = "Is translation needed?", showCondition = "confidential=\"DO_NOT_SHOW\"", searchable = false)
    private final LanguageTranslationRequirement translationRequirements;
    @CCD(label = "Document contains a confidential address?", searchable = false, typeOverride = FieldType.YesOrNo)
    private String hasConfidentialAddress;
    @CCD(
            label = "Choose an expert report type",
            showCondition = "type=\"EXPERT_REPORTS\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ExpertReportList"
    )
    private ExpertReportType expertReportType;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge",
            typeParameterClass = DocumentAcknowledge.class
    )
    private List<String> documentAcknowledge;
    @CCD(label = "Reason for removal", typeOverride = FieldType.TextArea)
    private String removalReason;
    @CCD(label = "Is confidential?", typeOverride = FieldType.YesOrNo)
    private String markAsConfidential;

    public String getHasConfidentialAddress() {
        return ((!isBlank(name) || document != null) && (!YesNo.isYesOrNo(hasConfidentialAddress)))
            ? YesNo.NO.getValue() : hasConfidentialAddress;
    }

    @JsonIgnore
    public boolean isConfidentialDocument() {
        return (confidential != null && confidential.contains("CONFIDENTIAL"))
               || YesNo.YES.getValue().equalsIgnoreCase(getHasConfidentialAddress());
    }

    @JsonIgnore
    public boolean isUploadedByHMCTS() {
        return "HMCTS".equals(uploadedBy);
    }

    @JsonIgnore
    public boolean isUploadedByRepresentativeSolicitor() {
        return "Yes".equals(uploadedBySolicitor);
    }

    @JsonGetter("confidentialTabLabel")
    public String generateConfidentialTabLabel() {
        return isConfidentialDocument() ? "Confidential" : null;
    }

    @Override
    public String asLabel() {
        return String.format("%s - %s - %s", Optional.ofNullable(type)
            .map(FurtherEvidenceType::getLabel)
            .orElse("Document"), name, formatLocalDateTimeBaseUsingFormat(dateTimeUploaded, DATE));
    }

    @Override
    @JsonIgnore
    public String getModifiedItemType() {
        return ModifiedOrderType.ANY_DOCUMENT.getLabel();
    }

    @Override
    @JsonIgnore
    public List<Element<Other>> getSelectedOthers() {
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public boolean hasBeenTranslated() {
        return Objects.nonNull(translatedDocument);
    }

    @JsonIgnore
    public boolean sentForTranslation() {
        return getNeedTranslation() == YesNo.YES && !hasBeenTranslated();
    }

    @Override
    public LocalDateTime translationUploadDateTime() {
        return translationUploadDateTime;
    }

    public ExpertReportType getExpertReportType() {
        if (!isNull(expertReportType)) {
            // if we have an expert report type set use that
            return expertReportType;
        } else if (FurtherEvidenceType.EXPERT_REPORTS.equals(type)) {
            // otherwise, if it's an expert report without a type, use generic 'other'
            return ExpertReportType.OTHER_EXPERT_REPORT;
        } else {
            // otherwise, it's not an expert report - so don't fill in this field
            return null;
        }
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
  private String documentAcknowledgeLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabelForCYA;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String confidentialWarningLabel;
  @CCD(label = " ", showCondition = "confidential CONTAINS \"CONFIDENTIAL\"", searchable = false)
  private String confidentialTabLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String hasConfidentialAddressLabel;
  // ==== end synthesised definition-only fields ====
}
