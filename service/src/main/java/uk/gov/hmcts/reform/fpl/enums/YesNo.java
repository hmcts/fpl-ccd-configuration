package uk.gov.hmcts.reform.fpl.enums;

public enum YesNo {
    YES, NO;

    public static YesNo from(boolean val) {
        return val ? YES : NO;
    }
}
