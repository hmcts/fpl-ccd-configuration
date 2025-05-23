package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode
public class CafcassApiCaseDocument {
    private String documentFileName;
    private boolean removed;
    private String documentCategory;
    private String documentId;
    private LocalDateTime uploadTimestamp;
}
