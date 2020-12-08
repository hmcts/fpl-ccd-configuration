package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisSocialWorkOther;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisAnnexDocuments {
    @Deprecated
    private final String socialWorkChronology;
    @Deprecated
    private final String socialWorkStatement;
    @Deprecated
    private final String socialWorkAssessment;
    @Deprecated
    private final String socialWorkCarePlan;
    @Deprecated
    private final String socialWorkEvidenceTemplate;
    @Deprecated
    private final String thresholdDocument;
    @Deprecated
    private final String checklistDocument;
    @Deprecated
    private final List<DocmosisSocialWorkOther> others;

    private final boolean featureToggleOn;
    private final List<DocmosisAnnexDocument> documents;
    private final String toFollowReason;
}
