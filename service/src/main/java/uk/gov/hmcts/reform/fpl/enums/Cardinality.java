package uk.gov.hmcts.reform.fpl.enums;

public enum Cardinality {
    ZERO, ONE, MANY;

    public static Cardinality from(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException("Cardinality can be represented by non negative numbers only");
        }

        switch (amount) {
            case 0:
                return ZERO;
            case 1:
                return ONE;
            default:
                return MANY;
        }
    }
}
