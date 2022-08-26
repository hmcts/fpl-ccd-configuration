package uk.gov.hmcts.reform.fpl.utils.elasticsearch;

import lombok.Getter;

public enum SortOrder {
    ASC("asc"),
    DESC("desc");

    @Getter
    private final String order;

    SortOrder(String order) {
        this.order = order;
    }
}
