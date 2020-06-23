package uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists;

import uk.gov.hmcts.ccd.sdk.types.FixedList;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@FixedList(generate = true, id = "CloseCaseFull")
public enum CloseCaseReason implements HasLabel {
    FINAL_ORDER("Final order issued"),
    REFUSAL("Refusal"),
    WITHDRAWN("Withdrawn"),
    NO_ORDER("No order was made"),
    DEPRIVATION_OF_LIBERTY("Deprivation of liberty"),
    OTHER("Other");

    private String label;

    CloseCaseReason(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

}
