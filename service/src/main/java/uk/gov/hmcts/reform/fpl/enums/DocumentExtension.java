package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum DocumentExtension {
    PDF("pdf");

    private final String label;

    DocumentExtension(String label) {
        this.label = label;
    }
}
