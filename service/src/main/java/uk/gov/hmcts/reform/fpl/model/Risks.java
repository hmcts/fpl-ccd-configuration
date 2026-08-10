package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.FactorsAffectingParentingType;
import uk.gov.hmcts.reform.fpl.enums.RiskAndHarmToChildrenType;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RiskAndHarm", generate = true)
@Data
@Builder
@AllArgsConstructor
public class Risks {
    @CCD(
            label = "Neglect",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String neglect;
    @CCD(
            label = "Sexual abuse",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String sexualAbuse;
    @CCD(
            label = "Physical harm including non-accidental injury",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String physicalHarm;
    @CCD(
            label = "Emotional harm",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String emotionalHarm;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2303, historical field)
     */
    @CCD(
            label = "Select all that apply",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "PastOrFutureHarmSelect",
            typeParameterClass = PastOrFutureHarmSelect.class
    )
    @Deprecated(since = "DFPL-2303")
    private final List<String> neglectOccurrences;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2303, historical field)
     */
    @CCD(
            label = "Select all that apply",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "PastOrFutureHarmSelect",
            typeParameterClass = PastOrFutureHarmSelect.class
    )
    @Deprecated(since = "DFPL-2303")
    private final List<String> sexualAbuseOccurrences;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2303, historical field)
     */
    @CCD(
            label = "Select all that apply",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "PastOrFutureHarmSelect",
            typeParameterClass = PastOrFutureHarmSelect.class
    )
    @Deprecated(since = "DFPL-2303")
    private final List<String> physicalHarmOccurrences;
    /**
     * This historical field is deprecated since DFPL-2303.
     * @deprecated (DFPL-2303, historical field)
     */
    @CCD(
            label = "Select all that apply",
            showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "PastOrFutureHarmSelect",
            typeParameterClass = PastOrFutureHarmSelect.class
    )
    @Deprecated(since = "DFPL-2303")
    private final List<String> emotionalHarmOccurrences;
    @CCD(
            label = "What kind of harm is the child at risk of?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "RiskAndHarmToChildrenList"
    )
    private final List<RiskAndHarmToChildrenType> whatKindOfRiskAndHarmToChildren;
    @CCD(
            label = "Is there anything affecting any respondent's ability to parent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FactorsAffectingParentingList"
    )
    private final List<FactorsAffectingParentingType> factorsAffectingParenting;
    @CCD(
            label = "Tell us what else is affecting their ability to parent",
            showCondition = "factorsAffectingParenting CONTAINS \"ANYTHING_ELSE\"",
            typeOverride = FieldType.TextArea
    )
    private final String anythingElseAffectingParenting;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "## Is there evidence of any of the following?",
          showCondition = "whatKindOfRiskAndHarmToChildren=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String evidenceQuestion;
  // ==== end synthesised definition-only fields ====
}
