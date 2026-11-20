package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum C2ApplicationRouteType {
    APPLY_ONLINE("Apply online"),
    PAPER_FORM("Upload a paper form");

    private final String label;
}
