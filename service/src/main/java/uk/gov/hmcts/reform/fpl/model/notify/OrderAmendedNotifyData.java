package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Data
public class OrderAmendedNotifyData extends BaseCaseNotifyData {
    private final String orderType;
    private final String callout;
    private final String courtName;
    private final Object documentLink; //This will be String or Map<String, Object> depending on case access/no access
}
