package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.ccd.sdk.types.HasLabel;

public enum CMOStatus implements HasLabel {
    SEND_TO_JUDGE("Yes, send this to the judge"),
    PARTIES_REVIEW("No, parties need to review it"),
    SELF_REVIEW("No, I need to make changes");

    private String label;

    CMOStatus(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
