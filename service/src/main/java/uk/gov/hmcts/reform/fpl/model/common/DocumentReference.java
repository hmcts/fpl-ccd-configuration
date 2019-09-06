package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocumentReference {
    @SuppressWarnings("membername")
    private final String document_url;
    @SuppressWarnings("membername")
    private final String document_filename;
    @SuppressWarnings("membername")
    private final String document_binary_url;
}
