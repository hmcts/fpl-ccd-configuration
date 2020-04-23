package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisSocialWorkOther;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DocmosisAnnexDocuments {
    private final String socialWorkChronology;
    private final String socialWorkStatement;
    private final String socialWorkAssessment;
    private final String socialWorkCarePlan;
    private final String socialWorkEvidenceTemplate;
    private final String thresholdDocument;
    private final String checklistDocument;
    private final List<DocmosisSocialWorkOther> others;
}
