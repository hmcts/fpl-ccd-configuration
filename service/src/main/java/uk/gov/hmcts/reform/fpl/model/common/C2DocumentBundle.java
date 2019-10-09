package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2DocumentBundle {
    private final DocumentReference c2upload;
    private final String c2UploadDescription;
}
