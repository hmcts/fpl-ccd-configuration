package uk.gov.hmcts.reform.aac.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApprovalStatus {

    PENDING("0"),
    APPROVED("1"),
    REJECTED("2");

    String value;

    ApprovalStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
