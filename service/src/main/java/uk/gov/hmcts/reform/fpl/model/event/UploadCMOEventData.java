package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadCMOEventData {

    // Uploaded document
    DocumentReference uploadedCaseManagementOrder;

    // Dynamic lists
    Object hearingsWithoutApprovedCMO;

    // Readonly info
    String cmoJudgeInfo;
    String cmoHearingInfo;
    String multiHearingsWithCMOs;
    String singleHearingWithCMO;

    // Conditional control fields
    NumberOfHearingsOptions numHearingsWithoutCMO;
    YesNo showHearingsSingleTextArea;
    YesNo showHearingsMultiTextArea;

    public static String[] transientFields() {
        return new String[]{
            "uploadedCaseManagementOrder", "hearingsWithoutApprovedCMO", "cmoJudgeInfo", "cmoHearingInfo",
            "numHearingsWithoutCMO", "singleHearingWithCMO", "multiHearingsWithCMOs", "showHearingsSingleTextArea",
            "showHearingsMultiTextArea"
        };
    }

    public enum NumberOfHearingsOptions {
        SINGLE,
        MULTI,
        NONE
    }
}

