package uk.gov.hmcts.reform.fpl.model.cfv;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

@Value
@Builder
public class UploadBundle {
    DocumentReference document;
    DocumentUploaderType uploaderType;
    List<String> uploaderCaseRoles;
    boolean confidential;
}
