package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    SEALED("Sealed", "standard-directions-order.pdf"),
    DRAFT("Draft", "draft-standard-directions-order.pdf");

    private final String value;
    private final String documentTitle;

    OrderStatus(String value, String documentTitle) {
        this.value = value;
        this.documentTitle = documentTitle;
    }
}
