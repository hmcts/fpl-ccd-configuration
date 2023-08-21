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

@JsonSubTypes({
    @JsonSubTypes.Type(value = RespondentStatementV2.class)
})
@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupportingEvidenceBundle implements TranslatableItem, FurtherDocument, WithDocument {
    private final String name;
    private final String notes;
    @PastOrPresentDate(message = "Date received cannot be in the future")
    private final LocalDateTime dateTimeReceived;
    private LocalDateTime dateTimeUploaded;
    private final DocumentReference document;
    private DocumentUploaderType uploaderType;
    private List<CaseRole> uploaderCaseRoles;
    private String uploadedBy;
    private List<String> confidential;
    private FurtherEvidenceType type;
    private String uploadedBySolicitor;
    private final DocumentReference translatedDocument;
    private final LocalDateTime translationUploadDateTime;
    private final LanguageTranslationRequirement translationRequirements;
    private String hasConfidentialAddress;
    private ExpertReportType expertReportType;
    private List<String> documentAcknowledge;
    private String removalReason;
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

}
