package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class OrderIssuedNotifyData extends BaseCaseNotifyData {
    private final String orderType;
    private final String callout;
    private final String courtName;
    private final Object documentLink;

}
