package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.interfaces.UploaderInfo;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DraftOrder implements UploaderInfo {
    private String title;
    private DocumentReference document;
    private LocalDate dateUploaded;
    private DocumentUploaderType uploaderType;
    private List<CaseRole> uploaderCaseRoles;
}
