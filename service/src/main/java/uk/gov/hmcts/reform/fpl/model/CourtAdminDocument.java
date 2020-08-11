package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
public class CourtAdminDocument {
    private final String documentTitle;
    private final DocumentReference document;
}
