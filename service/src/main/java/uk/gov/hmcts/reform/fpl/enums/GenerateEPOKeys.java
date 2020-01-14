package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

public enum GenerateEPOKeys {
    EPO_REMOVAL_ADDRESS("epoRemovalAddress"),
    EPO_CHILDREN("epoChildren"),
    EPO_END_DATE("epoEndDate"),
    EPO_PHRASE("epoPhrase"),
    EPO_TYPE("epoType");

    @Getter
    private final String key;

    GenerateEPOKeys(String key) {
        this.key = key;
    }
}
