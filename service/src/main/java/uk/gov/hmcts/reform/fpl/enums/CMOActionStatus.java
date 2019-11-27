package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum CMOActionStatus {
    SEND_TO_ALL_PARTIES("Yes, it can be sealed and sent to parties", "case-management-order.pdf"),
    LOCAL_AUTHORITY_CHANGES("No, local authority needs to make changes", "draft-case-management-order.pdf"),
    SELF_REVIEW("No, I need to make changes", "draft-case-management-order.pdf");

    private final String actionedStatusValue;
    private final String documentTitle;

    CMOActionStatus(String actionedStatusValue, String documentTitle) {
        this.actionedStatusValue = actionedStatusValue;
        this.documentTitle = documentTitle;
    }
}
