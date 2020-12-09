package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.DocumentMetaData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
public class CourtBundle extends DocumentMetaData {
    private String hearing;
    private DocumentReference document;
}
