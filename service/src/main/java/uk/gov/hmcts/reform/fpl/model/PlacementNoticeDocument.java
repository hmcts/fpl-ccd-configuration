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

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlacementNoticeDocument implements WithDocument {

    private RecipientType type;
    private DocumentReference response;
    private String responseDescription;
    private String recipientName;
    private UUID respondentId;
    private List<String> documentAcknowledge;
    private DocumentUploaderType uploaderType;
    private List<CaseRole> uploaderCaseRoles;
    private String removalReason;
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

}
