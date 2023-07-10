package uk.gov.hmcts.reform.fpl.enums.cfv;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DocumentType {
    BUNDLE("bundle", true),
    THRESHOLD("threshold", true);

    @Getter
    private String baseFieldName;
    @Getter
    private boolean uploadable;
}
