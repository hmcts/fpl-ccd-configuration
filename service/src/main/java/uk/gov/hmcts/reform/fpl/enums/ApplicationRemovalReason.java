package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ApplicationRemovalReason {
    @CCD(label = "Duplicate")
    DUPLICATE("Duplicate"),
    @CCD(label = "Wrong case")
    WRONG_CASE("Wrong case"),
    @CCD(label = "Other (with details)")
    OTHER("Other");

    private final String label;

    ApplicationRemovalReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
