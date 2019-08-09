package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TypeOfDocument {
    private final String document_url;
    private final String document_filename;
    private final String document_binary_url;
}
