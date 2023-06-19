package uk.gov.hmcts.reform.fpl.enums;

public enum DirectionsOrderType {

    SDO("SDO", "standard directions order"),
    UDO("UDO", "urgent directions order");

    private String shortForm;
    private String longForm;

    DirectionsOrderType(final String shortForm, final String longForm) {
        this.shortForm = shortForm;
        this.longForm = longForm;
    }

    public String getShortForm() {
        return shortForm;
    }

    public String getLongForm() {
        return longForm;
    }
}
