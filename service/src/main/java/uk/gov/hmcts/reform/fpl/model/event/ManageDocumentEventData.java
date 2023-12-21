package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ManageDocumentEventData {
    @Temp
    ManageDocumentAction manageDocumentAction;
    @Temp
    ManageDocumentRemovalReason manageDocumentRemoveDocReason;
    @Temp
    String manageDocumentRemoveDocAnotherReason;
    @Temp
    List<Element<UploadableDocumentBundle>> uploadableDocumentBundle;
    @Temp
    String hasConfidentialParty;
    @Temp
    String askForPlacementNoticeRecipientType;
    @Temp
    List<String> documentAcknowledge;
    @Temp
    String allowMarkDocumentConfidential;
    @Temp
    String allowSelectDocumentTypeToRemoveDocument;
    @Temp
    private DynamicList availableDocumentTypesForRemoval;
    @Temp
    private DynamicList documentsToBeRemoved;

    public static List<String> temporaryFields() {
        List<String> tempFields = getFieldsListWithAnnotation(ManageDocumentEventData.class, Temp.class).stream()
            .map(Field::getName)
            .toList();
        return tempFields;
    }

    public List<Element<UploadableDocumentBundle>> getUploadableDocumentBundle() {
        return defaultIfNull(this.uploadableDocumentBundle, new ArrayList<>());
    }

    public DocumentType getSelectedDocumentTypeToRemove() {
        if (getAvailableDocumentTypesForRemoval() != null && getAvailableDocumentTypesForRemoval().getValue() != null
            && !StringUtils.isEmpty(getAvailableDocumentTypesForRemoval().getValue().getCode())) {
            return DocumentType.valueOf(getAvailableDocumentTypesForRemoval().getValue().getCode());
        }
        return null;
    }

}
