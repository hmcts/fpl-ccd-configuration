package uk.gov.hmcts.reform.fpl.model.cfv;

import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadBundle {
    DocumentReference document;
    DocumentUploaderType uploaderType;
    boolean confidential;
}
