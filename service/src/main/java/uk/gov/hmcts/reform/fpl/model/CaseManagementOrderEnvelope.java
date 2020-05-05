package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import uk.gov.hmcts.reform.document.domain.Document;

@Data
public class CaseManagementOrderEnvelope {
    private final CaseManagementOrder order;
    private final Document document;
}
