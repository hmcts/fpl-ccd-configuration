package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UploadCMOEventData {

    // Uploaded documents (Page 2)
    DocumentReference uploadedCaseManagementOrder;
    DocumentReference replacementCMO;
    List<Element<SupportingEvidenceBundle>> cmoSupportingDocs;

    // Dynamic lists (Page 1)
    Object pastHearingsForCMO;
    Object futureHearingsForCMO;

    // Approved or draft CMO (Page 1)
    CMOType cmoUploadType;

    // Readonly info
    String cmosSentToJudge; // (Page 1)
    String cmoHearingInfo; // (Page 2)
    DocumentReference previousCMO; // (Page 2)
    String cmoJudgeInfo; // (Page 3)
    DocumentReference cmoToSend; // (Page 3)

    // Conditional control fields
    YesNo showCMOsSentToJudge; // (Page 1)
    YesNo showReplacementCMO; // (Page 2)

    public List<Element<SupportingEvidenceBundle>> getCmoSupportingDocs() {
        return defaultIfNull(cmoSupportingDocs, new ArrayList<>());
    }

    @JsonIgnore
    public boolean isAgreed() {
        return AGREED == cmoUploadType;
    }

    @JsonIgnore
    public UUID getSelectedHearingId(ObjectMapper mapper) {
        Object dynamicList = defaultIfNull(pastHearingsForCMO, futureHearingsForCMO);
        return getDynamicListSelectedValue(dynamicList, mapper);
    }

    public static String[] transientFields() {
        return new String[]{
            "showCMOsSentToJudge", "cmosSentToJudge", "cmoUploadType", "pastHearingsForCMO", "futureHearingsForCMO",
            "cmoHearingInfo", "showReplacementCMO", "previousCMO", "uploadedCaseManagementOrder", "replacementCMO",
            "cmoSupportingDocs", "cmoJudgeInfo", "cmoToSend"
        };
    }
}

