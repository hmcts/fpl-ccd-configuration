package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class HearingFurtherEvidenceBundle {
    private final String hearingName;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
}
