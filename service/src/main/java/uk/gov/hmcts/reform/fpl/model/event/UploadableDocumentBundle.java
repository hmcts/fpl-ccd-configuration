package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.PlacementNoticeRecipientType;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadableDocumentBundle {
    private DynamicList documentTypeDynamicList;
    private DocumentReference document;
    private String confidential;
    private DynamicList placementList;
    private PlacementNoticeRecipientType placementNoticeRecipientType;
    private LanguageTranslationRequirement translationRequirements;

    public DocumentType getDocumentTypeSelected() {
        if (getDocumentTypeDynamicList() != null && getDocumentTypeDynamicList().getValue() != null
            && !StringUtils.isEmpty(getDocumentTypeDynamicList().getValue().getCode())) {
            return DocumentType.valueOf(getDocumentTypeDynamicList().getValue().getCode());
        }
        return null;
    }
}
