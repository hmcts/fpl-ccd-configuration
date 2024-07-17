package uk.gov.hmcts.reform.fpl.model.cafcass.api;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CafcassApiCaseDocument {
    private String document_filename;
    private boolean removed;
    private String documentCategory;
    private String documentId;
}
