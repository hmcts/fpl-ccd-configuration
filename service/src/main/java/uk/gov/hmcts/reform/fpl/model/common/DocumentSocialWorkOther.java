package uk.gov.hmcts.reform.fpl.model.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class DocumentSocialWorkOther extends DocumentMetaData {
    private final String documentTitle;
}
