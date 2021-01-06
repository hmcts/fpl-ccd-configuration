package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UploadDraftOrdersData {

    List<HearingOrderKind> hearingOrderDraftKind;
    List<Element<HearingOrder>> currentHearingOrderDrafts;

    DocumentReference uploadedCaseManagementOrder;
    DocumentReference replacementCMO;
    List<Element<SupportingEvidenceBundle>> cmoSupportingDocs;

    Object pastHearingsForCMO;
    Object futureHearingsForCMO;
    Object hearingsForHearingOrderDrafts;

    CMOType cmoUploadType;

    String cmosSentToJudge;
    String cmoHearingInfo;
    DocumentReference previousCMO;
    String cmoJudgeInfo;
    DocumentReference cmoToSend;

    YesNo showCMOsSentToJudge;
    YesNo showReplacementCMO;

    public List<Element<SupportingEvidenceBundle>> getCmoSupportingDocs() {
        return defaultIfNull(cmoSupportingDocs, new ArrayList<>());
    }

    @JsonIgnore
    public boolean isAgreed() {
        return AGREED == cmoUploadType;
    }

    public static String[] transientFields() {
        return new String[]{
            "showCMOsSentToJudge", "cmosSentToJudge", "cmoUploadType", "pastHearingsForCMO", "futureHearingsForCMO",
            "cmoHearingInfo", "showReplacementCMO", "previousCMO", "uploadedCaseManagementOrder", "replacementCMO",
            "cmoSupportingDocs", "cmoJudgeInfo", "cmoToSend", "hearingsForHearingOrderDrafts",
            "currentHearingOrderDrafts", "hearingOrderDraftKind"
        };
    }
}

