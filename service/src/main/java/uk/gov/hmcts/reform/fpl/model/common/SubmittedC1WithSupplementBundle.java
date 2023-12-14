package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SubmittedC1WithSupplementBundle  {
    private final DocumentReference document;
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    private List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private final List<Element<Supplement>> supplementsBundle;
}
