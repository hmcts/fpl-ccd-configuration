package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

@Data
@Builder
public class ManageDocument {
    private final ManageDocumentType type;
    private final String relatedToHearing;
    private DynamicList hearingList;
    private DynamicList supportingC2List;
}
