package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Data
public class ApplicationRemovedNotifyData extends BaseCaseNotifyData {
    private final String caseId;
    private final String c2Filename;
    private final String applicantName;
    private final String removalDate;
    private final String reason;
    private final String applicationFeeText;
    private final String childLastName;
}
