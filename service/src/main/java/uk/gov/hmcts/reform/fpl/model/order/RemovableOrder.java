package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Data;

@Data
public abstract class RemovableOrder {
    private String removalReason;

    public abstract String asLabel();
}
