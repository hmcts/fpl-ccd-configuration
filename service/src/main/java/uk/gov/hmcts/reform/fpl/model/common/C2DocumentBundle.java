package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class C2DocumentBundle {
    private final DocumentReference document;
    private final String description;
    private final String uploadedDateTime;
    private final String author;
}
