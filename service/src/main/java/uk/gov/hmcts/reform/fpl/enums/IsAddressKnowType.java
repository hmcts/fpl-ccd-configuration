package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum IsAddressKnowType {
    YES("Yes"),
    NO("No"),
    LIVE_IN_REFUGEE("LIVE_IN_REFUGE");

    private final String value;

    public IsAddressKnowType fromString(String strValue) {
        return Arrays.stream(IsAddressKnowType.values()).filter(enumEntry ->
                enumEntry.toString().equalsIgnoreCase(strValue) || enumEntry.getValue().equalsIgnoreCase(strValue))
            .findFirst().orElse(null);
    }
}
