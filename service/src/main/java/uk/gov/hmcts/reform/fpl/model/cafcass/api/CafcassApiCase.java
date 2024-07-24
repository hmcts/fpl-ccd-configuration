package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
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
