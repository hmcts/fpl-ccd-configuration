package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;

@Value
@Builder(toBuilder = true)
public class UploadDraftOrdersData {

    @Temp
    List<HearingOrderKind> hearingOrderDraftKind;
    @Temp
    List<Element<HearingOrder>> currentHearingOrderDrafts;

    @Temp
    DocumentReference uploadedCaseManagementOrder;
    @Temp
    DocumentReference replacementCMO;
    @Temp
    List<Element<SupportingEvidenceBundle>> cmoSupportingDocs;

    @Temp
    Object pastHearingsForCMO;
    @Temp
    Object futureHearingsForCMO;
    @Temp
    Object hearingsForHearingOrderDrafts;

    @Temp
    CMOType cmoUploadType;

    @Temp
    String cmosSentToJudge;
    @Temp
    String cmoHearingInfo;
    @Temp
    DocumentReference previousCMO;
    @Temp
    String cmoJudgeInfo;
    @Temp
    DocumentReference cmoToSend;

    @Temp
    YesNo showCMOsSentToJudge;
    @Temp
    YesNo showReplacementCMO;

    public List<Element<SupportingEvidenceBundle>> getCmoSupportingDocs() {
        return defaultIfNull(cmoSupportingDocs, new ArrayList<>());
    }

    @JsonIgnore
    public boolean isCmoAgreed() {
        return AGREED == cmoUploadType;
    }

    public static String[] temporaryFields() {
        return getFieldsListWithAnnotation(UploadDraftOrdersData.class, Temp.class).stream()
            .map(Field::getName)
            .toArray(String[]::new);
    }

    @JsonIgnore
    public Object getHearingDynamicList() {
        if (isEmpty(hearingOrderDraftKind)) {
            return null;
        }

        if (hearingOrderDraftKind.contains(CMO)) {
            if (cmoUploadType == AGREED) {
                return pastHearingsForCMO;
            }
            if (cmoUploadType == DRAFT) {
                return futureHearingsForCMO;
            }
        } else {
            return hearingsForHearingOrderDrafts;
        }

        return null;
    }
}
