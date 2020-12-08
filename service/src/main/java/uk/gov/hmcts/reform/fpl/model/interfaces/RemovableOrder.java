package uk.gov.hmcts.reform.fpl.model.interfaces;

public interface RemovableOrder {
    boolean isRemovable();

    String asLabel();

    void setRemovalReason(String reason);
}
