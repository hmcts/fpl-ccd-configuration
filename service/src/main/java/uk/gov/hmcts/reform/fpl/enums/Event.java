package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum Event {
    DRAFT_CASE_MANAGEMENT_ORDER("draftCMO"),
    ACTION_CASE_MANAGEMENT_ORDER("actionCMO");

    private final String id;

    Event(String id) {
        this.id = id;
    }
}
