package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CaseSummary extends HearingDocument {

    @Builder(toBuilder = true)
    public CaseSummary(DocumentReference document,
                       LocalDateTime dateTimeUploaded,
                       String uploadedBy,
                       String hearing,
                       String hasConfidentialAddress,
                       DocumentUploaderType uploaderType,
                       List<CaseRole> uploaderCaseRoles,
                       String markAsConfidential) {
        super.dateTimeUploaded = dateTimeUploaded;
        super.uploadedBy = uploadedBy;
        super.hearing = hearing;
        super.document = document;
        super.hasConfidentialAddress = hasConfidentialAddress;
        super.uploaderType = uploaderType;
        super.uploaderCaseRoles = uploaderCaseRoles;
        super.markAsConfidential = markAsConfidential;
    }
}
