package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagingOrganisationRemovedNotifyData implements NotifyData {
    private final Long caseNumber;
    private final String caseName;
    private final String localAuthorityName;
    private final String managingOrganisationName;
}
