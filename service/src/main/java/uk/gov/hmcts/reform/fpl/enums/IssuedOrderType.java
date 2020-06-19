package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IssuedOrderType {
    GENERATED_ORDER("Order"),
    NOTICE_OF_PLACEMENT_ORDER("Notice of placement order"),
    CMO("Case management order");

    private final String label;
}
