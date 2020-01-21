package uk.gov.hmcts.reform.fpl.enums.ccd.casefields;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum GeneratedEPOKey implements CaseField {
    EPO_REMOVAL_ADDRESS("epoRemovalAddress"),
    EPO_CHILDREN("epoChildren"),
    EPO_END_DATE("epoEndDate"),
    EPO_PHRASE("epoPhrase"),
    EPO_TYPE("epoType");

    private final String key;

    public static Stream<GeneratedEPOKey> asStream() {
        return Arrays.stream(GeneratedEPOKey.values());
    }
}
