package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Document {
    private final String statusReason;
    private final String documentStatus;
    private final TypeOfDocument typeOfDocument;
}
