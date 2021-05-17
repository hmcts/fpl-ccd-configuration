package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ScannedDocument {
    private final String type;
    private final String subtype;
    private final DocumentReference url;
    private final String controlNumber;
    private final String fileName;
    private final LocalDateTime scannedDate;
    private final LocalDateTime deliveryDate;
    private final String exceptionRecordReference;
}
