package uk.gov.hmcts.reform.fpl.config.utils;

public enum FutureOrPastHarmSelectType {

    PAST_HARM("Past harm"),
    FUTURE_RISK_OF_HARM("Future risk of harm");

    private final String label;

    FutureOrPastHarmSelectType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
