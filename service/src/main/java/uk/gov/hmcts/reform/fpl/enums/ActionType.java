package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    SEND_TO_ALL_PARTIES("Yes, it can be sealed and sent to parties"),
    JUDGE_REQUESTED_CHANGE("No, local authority needs to make changes"),
    SELF_REVIEW("No, I need to make changes");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }
}
