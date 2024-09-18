package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CafcassApiCase {
    private Long caseId;
    private String jurisdiction;
    private String state;
    private String caseTypeId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String lastStateModifiedDate;
    private CafcassApiCaseData caseData;
}
