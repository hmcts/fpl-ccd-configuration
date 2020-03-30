package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;

@Data
@Builder(toBuilder = true)
public class C2DocumentBundle {
    private final C2ApplicationType type;
    private final String nameOfRepresentative;
    private final String pbaNumber;
    private final String clientCode;

    @JsonProperty(value = "fileReference")
    private final String customerReference;

    private final DocumentReference document;
    private final String description;
    private final String uploadedDateTime;
    private final String author;
}
