package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CourtServicesNeeded;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
public class HearingPreferences {
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Spoken or written Welsh",
            showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2316")
    private final String welsh;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Interpreter",
            hint = "Including sign language",
            showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2316")
    private final String interpreter;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(label = "Intermediary", showCondition = "whichCourtServices=\"DO_NOT_SHOW\"", typeOverride = FieldType.YesOrNo)
    @Deprecated(since = "DFPL-2316")
    private final String intermediary;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Give details",
            showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2316")
    private final String welshDetails;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Facilities or assistance for a disability",
            showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2316")
    private final String disabilityAssistance;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Separate waiting room or other security measures",
            hint = "For example, mother and father need to be in separate waiting rooms as history of domestic violence",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2316")
    private final String extraSecurityMeasures;
    /**
     * This historical field is deprecated since DFPL-2316.
     * @deprecated (DFPL-2316, historical field)
     */
    @CCD(
            label = "Something else",
            showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    @Deprecated(since = "DFPL-2316")
    private final String somethingElse;
    @CCD(
            label = "Choose which court services you need to be considered before first hearing",
            hint = "Select all options that are relevant to you"
    )
    private final List<CourtServicesNeeded> whichCourtServices;
    @CCD(
            label = "Interpreter details",
            showCondition = "whichCourtServices CONTAINS \"INTERPRETER\"",
            typeOverride = FieldType.TextArea
    )
    private final String interpreterDetails;
    @CCD(
            label = "Intermediary details",
            showCondition = "whichCourtServices CONTAINS \"INTERMEDIARY\"",
            typeOverride = FieldType.TextArea
    )
    private final String intermediaryDetails;
    @CCD(
            label = "Disability facilities and assistance details",
            showCondition = "whichCourtServices CONTAINS \"FACILITIES_FOR_DISABILITY\"",
            typeOverride = FieldType.TextArea
    )
    private final String disabilityAssistanceDetails;
    @CCD(
            label = "Separate waiting rooms details",
            showCondition = "whichCourtServices CONTAINS \"SEPARATE_WAITING_ROOMS\"",
            typeOverride = FieldType.TextArea
    )
    private final String extraSecurityMeasuresDetails;
    @CCD(
            label = "What else is needed?",
            showCondition = "whichCourtServices CONTAINS \"SOMETHING_ELSE\"",
            typeOverride = FieldType.TextArea
    )
    private final String somethingElseDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Is any of the following needed to help someone take part in a hearing?",
          showCondition = "whichCourtServices=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String hearingPreferencesLabel;
  @CCD(label = "Litigation capacity issues", showCondition = "whichCourtServices=\"DO_NOT_SHOW\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo litigation;
  @CCD(label = "Give details", showCondition = "whichCourtServices=\"DO_NOT_SHOW\"", typeOverride = FieldType.TextArea)
  private String litigationDetails;
  @CCD(
          label = "Learning disability issues",
          hint = "For example, Respondent has learning disability",
          showCondition = "whichCourtServices=\"DO_NOT_SHOW\""
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo learningDisability;
  @CCD(label = "Give details", showCondition = "whichCourtServices=\"DO_NOT_SHOW\"", typeOverride = FieldType.TextArea)
  private String learningDisabilityDetails;
  // ==== end synthesised definition-only fields ====
}
