package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
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
