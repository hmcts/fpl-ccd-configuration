package uk.gov.hmcts.reform.fpl.model.api.cafcass;

import lombok.Builder;

@Builder
public class CafcassApiCase {
    private String caseId;
    private String jurisdiction;
    private String state;
    private String caseTypeId;
    private String createdDate;
    private String lastModified;
    private String lastStateModifiedDate;
    private CafcassApiCaseData caseData;
}
