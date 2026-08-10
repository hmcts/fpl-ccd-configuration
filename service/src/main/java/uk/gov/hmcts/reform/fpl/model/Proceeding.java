package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Proceeding {
    @CCD(
            label = "Are there any past or ongoing proceedings relevant to this case?",
            hint = "This should include any criminal proceedings involving respondents",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "RelevantProceedings",
            typeParameterClass = RelevantProceedings.class
    )
    private final String onGoingProceeding;
    @CCD(
            label = "Are these previous or ongoing proceedings?",
            showCondition = "onGoingProceeding=\"Yes\"",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "ProceedingStatus",
            typeParameterClass = ProceedingStatus.class
    )
    private final String proceedingStatus;
    @CCD(label = "Case number", showCondition = "onGoingProceeding=\"Yes\"")
    private final String caseNumber;
    @CCD(label = "Date started", showCondition = "onGoingProceeding=\"Yes\"")
    private final String started;
    @CCD(label = "Date ended", showCondition = "onGoingProceeding=\"Yes\"")
    private final String ended;
    @CCD(label = "Orders made", showCondition = "onGoingProceeding=\"Yes\"")
    private final String ordersMade;
    @CCD(
            label = "Judge",
            hint = "Include level, for example District Judge Martin Brown",
            showCondition = "onGoingProceeding=\"Yes\""
    )
    private final String judge;
    @CCD(
            label = "Names of children involved",
            showCondition = "onGoingProceeding=\"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String children;
    @CCD(label = "Name of guardian", showCondition = "onGoingProceeding=\"Yes\"")
    private final String guardian;
    @CCD(
            label = "Is the same guardian needed?",
            showCondition = "onGoingProceeding=\"Yes\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String sameGuardianNeeded;
    @CCD(label = "Give reason", showCondition = "sameGuardianNeeded=\"No\"", typeOverride = FieldType.TextArea)
    private final String sameGuardianDetails;
    @CCD(
            label = "Additional proceedings",
            showCondition = "onGoingProceeding=\"Yes\"",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProceedingType"
    )
    private final List<Element<Proceeding>> additionalProceedings;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Other proceedings 1", showCondition = "onGoingProceeding=\"Yes\"", typeOverride = FieldType.Label)
  private String proceedingLabel;
  // ==== end synthesised definition-only fields ====
}
