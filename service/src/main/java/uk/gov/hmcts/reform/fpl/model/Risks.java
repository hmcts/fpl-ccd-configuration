package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.FactorsAffectingParentingType;
import uk.gov.hmcts.reform.fpl.enums.RiskAndHarmToChildrenType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Risks {
    private final String neglect;
    private final String sexualAbuse;
    private final String physicalHarm;
    private final String emotionalHarm;
    @Deprecated
    private final List<String> neglectOccurrences;
    @Deprecated
    private final List<String> sexualAbuseOccurrences;
    @Deprecated
    private final List<String> physicalHarmOccurrences;
    @Deprecated
    private final List<String> emotionalHarmOccurrences;
    private final List<RiskAndHarmToChildrenType> whatKindOfRiskAndHarmToChildren;
    private final List<FactorsAffectingParentingType> factorsAffectingParenting;
    private final String anythingElseAffectingParenting;
}
