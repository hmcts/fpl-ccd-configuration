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

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicationDocument implements FurtherDocument, WithDocument {
    private String allowMarkDocumentConfidential;
    private final DocumentReference document;
    private DocumentUploaderType uploaderType;
    private List<CaseRole> uploaderCaseRoles;
    private final ApplicationDocumentType documentType;
    protected LocalDateTime dateTimeUploaded;
    private String uploadedBy;
    private String documentName;
    private String includedInSWET;
    private List<String> documentAcknowledge;
    private List<String> confidential;
    private String removalReason;
    private String markAsConfidential;
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
}
