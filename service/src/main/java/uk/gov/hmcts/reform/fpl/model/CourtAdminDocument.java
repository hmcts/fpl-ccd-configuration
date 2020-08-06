package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@RequiredArgsConstructor
public class CourtAdminDocument {
    private final String documentTitle;
    private final DocumentReference document;
}
