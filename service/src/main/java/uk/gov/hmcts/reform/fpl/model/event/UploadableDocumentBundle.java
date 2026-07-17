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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.NoConfidentialAddressConfirmation;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadableDocumentBundle {
    @CCD(label = "Document type", typeOverride = FieldType.DynamicList)
    private DynamicList documentTypeDynamicList;
    @CCD(label = "Upload a document", typeOverride = FieldType.Document)
    private DocumentReference document;
    @CCD(
            label = "Is this document confidential?",
            hint = "A document is confidential if it has names, addresses or any other information that could identify a person or their whereabouts that should not be disclosed to all parties",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private String confidential;
    @CCD(label = "Which placement application?", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList placementList;
    @CCD(
            label = "Recipient type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PlacementNoticeRecipientType"
    )
    private PlacementNoticeRecipientType placementNoticeRecipientType;
    @CCD(label = "Is translation needed?", searchable = false)
    private LanguageTranslationRequirement translationRequirements;

    public DocumentType getDocumentTypeSelected() {
        if (getDocumentTypeDynamicList() != null && getDocumentTypeDynamicList().getValue() != null
            && !StringUtils.isEmpty(getDocumentTypeDynamicList().getValue().getCode())) {
            return DocumentType.valueOf(getDocumentTypeDynamicList().getValue().getCode());
        }
        return null;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "There is a confidential address on this case. Check the document and confirm it doesn't contain a confidential address.",
          searchable = false
  )
  private java.util.Set<NoConfidentialAddressConfirmation> hasConfidentialAddress;
  // ==== end synthesised definition-only fields ====
}
