package uk.gov.hmcts.reform.fpl.model.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewDocumentData implements CafcassData {
    private String emailSubjectInfo;
    private String documentTypes;
    private String hearingDetails;
}
