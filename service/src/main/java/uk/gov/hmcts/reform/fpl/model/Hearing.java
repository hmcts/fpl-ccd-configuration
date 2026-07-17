package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.hearing.HearingUrgencyType;

import static java.util.Objects.nonNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Hearing {
    @CCD(label = "Hearing type")
    private final HearingUrgencyType hearingUrgencyType;
    @CCD(
            label = "Details and reason",
            showCondition = "hearingUrgencyType=\"SAME_DAY\" OR hearingUrgencyType=\"URGENT\"",
            typeOverride = FieldType.TextArea
    )
    private final String hearingUrgencyDetails;
    @CCD(
            label = "Do you need a without notice hearing?",
            showCondition = "hearingUrgencyType=\"SAME_DAY\" OR hearingUrgencyType=\"URGENT\"",
            typeOverride = FieldType.YesOrNo
    )
    private final String withoutNotice;
    @CCD(
            label = "Details and reason",
            showCondition = "hearingUrgencyType!=\"STANDARD\" AND withoutNotice=\"Yes\"",
            typeOverride = FieldType.TextArea
    )
    private final String withoutNoticeReason;
    @CCD(label = "Are respondents aware of proceedings?", typeOverride = FieldType.YesOrNo)
    private final String respondentsAware;
    @CCD(label = "Details and reason", showCondition = "respondentsAware=\"No\"", typeOverride = FieldType.TextArea)
    private final String respondentsAwareReason;

    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(
            label = "What type of hearing do you need?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingTypeFixedList"
    )
    @Deprecated(since = "DFPL-2304")
    private final String type;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(
            label = "Give reason",
            hint = "For example, need hearing before 30 July 2018 as the child is being released from hospital on 31 July 2018",
            showCondition = "timeFrame=\"Same day\" OR timeFrame=\"Other\"",
            typeOverride = FieldType.TextArea
    )
    @Deprecated(since = "DFPL-2304")
    private final String reason;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(
            label = "When do you need a hearing?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "TimeFrameList"
    )
    @Deprecated(since = "DFPL-2304")
    private final String timeFrame;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(label = "Do you need a hearing with reduced notice?", typeOverride = FieldType.YesOrNo)
    @Deprecated(since = "DFPL-2304")
    private final String reducedNotice;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(label = "Give reason", showCondition = "type=\"*\"", typeOverride = FieldType.TextArea)
    @Deprecated(since = "DFPL-2304")
    @JsonProperty("type_GiveReason")
    private final String typeGiveReason;
    /**
     * This historical hearing urgency field is deprecated since DFPL-2304.
     * @deprecated (DFPL-2304, historical hearing urgency field)
     */
    @CCD(label = "Give reason", showCondition = "reducedNotice=\"Yes\"", typeOverride = FieldType.TextArea)
    @Deprecated(since = "DFPL-2304")
    private final String reducedNoticeReason;

    /**
     * Use this method if backward compatible with historical data if required.
     * @return hearingUrgencyType if not null, otherwise return timeFrame
     */
    @JsonIgnore
    @SuppressWarnings("java:S1874")
    public String getHearingUrgencyTypeOrTimeFrame() {
        return nonNull(hearingUrgencyType) ? hearingUrgencyType.getLabel() : timeFrame;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Give reason and proposed dates",
          hint = "For example, need hearing before 30 July 2018 as the child is being released from hospital on 31 July 2018",
          showCondition = "timeFrame=\"Within 2 days\"",
          typeOverride = FieldType.TextArea
  )
  private String reason2Days;
  @CCD(
          label = "Give reason and proposed dates",
          hint = "For example, need hearing before 30 July 2018 as the child is being released from hospital on 31 July 2018",
          showCondition = "timeFrame=\"Within 7 days\"",
          typeOverride = FieldType.TextArea
  )
  private String reason7Days;
  @CCD(
          label = "Give reason and proposed dates",
          hint = "For example, need hearing before 30 July 2018 as the child is being released from hospital on 31 July 2018",
          showCondition = "timeFrame=\"Within 12 days\"",
          typeOverride = FieldType.TextArea
  )
  private String reason12Days;
  // ==== end synthesised definition-only fields ====
}
