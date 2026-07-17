package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
public class InternationalElement {
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Are you aware of any issues with the jurisdiction of this case - for example under the Brussels 2 regulation?",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2295")
    private final String issues;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Are you aware of any proceedings outside the UK?",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2295")
    private final String proceedings;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Give reason",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2295")
    private final String issuesReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Are there any suitable carers outside of the UK?",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarer;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Give reason",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2295")
    private final String proceedingsReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Are you aware of any significant events that have happened outside the UK?",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2295")
    private final String significantEvents;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Give reason",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2295")
    private final String possibleCarerReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Give reason",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2295")
    private final String significantEventsReason;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Has, or should, a government or central authority in another country been involved in this case?",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvement;
    /**
     * This historical field is deprecated since DFPL-2295.
     * @deprecated (DFPL-2295, historical field)
     */
    @CCD(
            label = "Give reason",
            showCondition = "whichCountriesInvolved=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2295")
    private final String internationalAuthorityInvolvementDetails;
    @CCD(label = "Which other countries are involved?", typeOverride = FieldType.TextArea)
    private final String whichCountriesInvolved;
    @CCD(label = "Are any of these countries outside of the Hague Convention?", typeOverride = FieldType.YesOrNo)
    private final String outsideHagueConvention;
    @CCD(
            label = "Provide all important details",
            hint = "Including any carers, events, proceedings or authorities outside the UK, or issues with jurisdiction.",
            typeOverride = FieldType.TextArea
    )
    private final String importantDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='panel panel-border-wide govuk-!-font-size-16'>Only complete this section if there is an international element to this application.</div>",
          typeOverride = FieldType.Label
  )
  private String internationalElementNotice;
  // ==== end synthesised definition-only fields ====
}
