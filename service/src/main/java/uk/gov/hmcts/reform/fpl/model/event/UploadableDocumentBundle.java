package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.PlacementNoticeRecipientType;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.Optional;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadableDocumentBundle {
    private DynamicList documentTypeDynamicList;
    private DocumentReference document;
    private String confidential;
    private DynamicList availablePlacements;
    private PlacementNoticeRecipientType placementNoticeRecipientType;

    public DocumentType getDocumentTypeSelected() {
        if (getDocumentTypeDynamicList() != null) {
            return DocumentType.valueOf(Optional.ofNullable(getDocumentTypeDynamicList().getValue())
                .orElse(DynamicListElement.builder().build())
                .getCode());
        }
        return null;
    }
}
