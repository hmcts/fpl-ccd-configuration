package uk.gov.hmcts.reform.fpl.model.interfaces;

public interface ApplicationsBundle extends ConfidentialBundle {

    String toLabel();

    int getSortOrder();
}
