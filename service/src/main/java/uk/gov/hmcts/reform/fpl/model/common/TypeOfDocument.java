package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
// Added supression to remove pattern match error - must match pattern '^[a-z][a-z0-9][a-zA-Z0-9]*$'
@SuppressWarnings("all")
public class TypeOfDocument {
    private final String document_url;
    private final String document_filename;
    private final String document_binary_url;
}
