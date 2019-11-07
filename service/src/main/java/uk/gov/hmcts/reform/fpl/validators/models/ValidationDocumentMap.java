package uk.gov.hmcts.reform.fpl.validators.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Document;

@Data
@Builder
public class ValidationDocumentMap {
    String key;
    Document document;
}
