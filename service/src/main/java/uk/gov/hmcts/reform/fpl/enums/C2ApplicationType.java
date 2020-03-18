package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.ccd.sdk.types.HasLabel;

public enum C2ApplicationType implements HasLabel {
    WITH_NOTICE("Application with notice. The other party will be notified about this application, even if there is no hearing."),
    WITHOUT_NOTICE("Application by consent or without notice. No notice will be sent to the other party, even if there is a hearing.");

    private String label;

    C2ApplicationType(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
