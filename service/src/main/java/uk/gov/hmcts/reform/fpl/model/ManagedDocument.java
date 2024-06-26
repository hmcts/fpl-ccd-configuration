package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class ManagedDocument implements WithDocument, NotifyDocumentUploaded {
    private DocumentReference document;
    private DocumentUploaderType uploaderType;
    private List<CaseRole> uploaderCaseRoles;
    @Setter
    private String removalReason;
    private String markAsConfidential;
    private final LanguageTranslationRequirement translationRequirements;
}
