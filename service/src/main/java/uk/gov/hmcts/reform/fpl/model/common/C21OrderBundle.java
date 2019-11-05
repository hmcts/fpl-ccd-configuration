package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C21OrderBundle {
    private final DocumentReference c21OrderDocument;
    private final String orderDate;
    private final String orderTitle;
    private final String judgeTitleAndName;
}
