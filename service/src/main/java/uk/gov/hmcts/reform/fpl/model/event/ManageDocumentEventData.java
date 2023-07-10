package uk.gov.hmcts.reform.fpl.model.event;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Value
@Jacksonized
@Builder
@JsonInclude(value = NON_NULL)
public class ManageDocumentEventData {
    @Temp
    ManageDocumentAction manageDocumentAction;
    @Temp
    List<Element<UploadableDocumentBundle>> uploadableDocumentBundle;

}
