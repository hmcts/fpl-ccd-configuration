package uk.gov.hmcts.reform.fpl.model.interfaces;

public interface SelectableItem {

    String toLabel();

    int getSortOrder();

    String getUploadedDateTime();
}
