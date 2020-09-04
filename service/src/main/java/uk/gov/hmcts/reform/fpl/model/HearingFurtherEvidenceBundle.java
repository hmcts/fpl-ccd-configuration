package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder
public class HearingFurtherEvidenceBundle {
    private final String hearingName;
    private final List<Element<ManageDocumentBundle>> manageDocumentBundle;
}
