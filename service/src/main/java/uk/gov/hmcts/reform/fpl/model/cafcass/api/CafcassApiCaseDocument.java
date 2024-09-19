package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode
public class CafcassApiCaseDocument {
    private String documentFileName;
    private boolean removed;
    private String documentCategory;
    private String documentId;
    private LocalDateTime uploadTimestamp;
}
