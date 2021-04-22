package uk.gov.hmcts.reform.fpl.model.noc;


public enum ChangeOfRepresentationMethod {
    NOC("Noc"),
    RESPONDENTS_EVENT("FPLA");

    private String label;

    ChangeOfRepresentationMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
