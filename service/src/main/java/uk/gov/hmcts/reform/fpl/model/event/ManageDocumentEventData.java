package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.lang.reflect.Field;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ManageDocumentEventData {
    @Temp
    ManageDocumentAction manageDocumentAction;
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

    public static List<String> temporaryFields() {
        List<String> tempFields = getFieldsListWithAnnotation(ManageDocumentEventData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(toList());
        return tempFields;
    }

}
