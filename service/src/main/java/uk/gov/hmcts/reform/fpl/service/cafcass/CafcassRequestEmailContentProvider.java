package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CafcassRequestEmailContentProvider {
    ORDER("new order",
        "A new order for this case was uploaded to the Public Law Portal entitled %s");

    private final String type;
    private final String content;
}
