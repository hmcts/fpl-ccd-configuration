package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    LanguageTranslationRequirement cmoToSendTranslationRequirements;
    @Temp
    DocumentReference orderToSend0;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements0;
    @Temp
    DocumentReference orderToSend1;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements1;
    @Temp
    DocumentReference orderToSend2;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements2;
    @Temp
    DocumentReference orderToSend3;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements3;
    @Temp
    DocumentReference orderToSend4;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements4;
    @Temp
    DocumentReference orderToSend5;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements5;
    @Temp
    DocumentReference orderToSend6;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements6;
    @Temp
    DocumentReference orderToSend7;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements7;
    @Temp
    DocumentReference orderToSend8;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements8;
    @Temp
    DocumentReference orderToSend9;
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements9;
    @Temp
    String orderToSendOptionCount;

    @Temp
    YesNo showCMOsSentToJudge;
    @Temp
    YesNo showReplacementCMO;
    @Temp
    List<String> uploadCMOMessageAcknowledge;

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
    public LanguageTranslationRequirement getOrderToSendTranslationRequirements(int i) {
        List<LanguageTranslationRequirement> orderToSendTranslationRequirements = Lists.newArrayList(
                orderToSendTranslationRequirements0,
                orderToSendTranslationRequirements1,
                orderToSendTranslationRequirements2,
                orderToSendTranslationRequirements3,
                orderToSendTranslationRequirements4,
                orderToSendTranslationRequirements5,
                orderToSendTranslationRequirements6,
                orderToSendTranslationRequirements7,
                orderToSendTranslationRequirements8,
                orderToSendTranslationRequirements9
            ).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return i < orderToSendTranslationRequirements.size() ? orderToSendTranslationRequirements.get(i) : null;
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
