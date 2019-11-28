package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum CMOActionType {
    SEND_TO_ALL_PARTIES("Yes, it can be sealed and sent to parties"),
    LOCAL_AUTHORITY_CHANGES("No, local authority needs to make changes"),
    SELF_REVIEW("No, I need to make changes");

    private final String actionTypeValue;

    CMOActionType(String actionTypeValue) {
        this.actionTypeValue = actionTypeValue;
    }
}
