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
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.AGREED;
import static uk.gov.hmcts.reform.fpl.enums.CMOType.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus25RolesDalfnpAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.model.DocumentAcknowledge;

@Value
@Builder(toBuilder = true)
public class UploadDraftOrdersData {

    @CCD(
            label = "What order are you adding?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DraftOrderKind",
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class, LABARRISTERCrudAccess.class}
    )
    @Temp
    List<HearingOrderKind> hearingOrderDraftKind;
    @CCD(
            label = "Order",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class, LABARRISTERCrudAccess.class}
    )
    @Temp
    List<Element<HearingOrder>> currentHearingOrderDrafts;

    @CCD(
            label = "Attach CMO",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class, LABARRISTERCruAccess.class}
    )
    @Temp
    DocumentReference uploadedCaseManagementOrder;
    @CCD(
            label = "Or upload an updated order",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference replacementCMO;

    @CCD(
            label = "Which hearing does this CMO relate to?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    Object pastHearingsForCMO;
    @CCD(
            label = "Which hearing does this CMO relate to?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    Object futureHearingsForCMO;
    @CCD(
            label = "Which hearing does the order relates to?",
            hint = "Choose 'No hearing' if it does not relate to hearing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CHILDSOLICITORACrudPlus25RolesDalfnpAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, BARRISTERCaseworkerPubliclawCourtadminCrudAccess.class, LABARRISTERCrudAccess.class}
    )
    @Temp
    Object hearingsForHearingOrderDrafts;

    @CCD(
            label = "Which CMO are you uploading, and what do you want the judge to do?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCaseworkerPubliclawCourtadminCruAccess.class, BARRISTERCrudAccess.class, SOLICITORCruAccess.class}
    )
    @Temp
    CMOType cmoUploadType;

    @CCD(
            label = "Hearings with CMOs already being reviewed",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    String cmosSentToJudge;
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    String cmoHearingInfo;
    @CCD(
            label = "Send this order",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference previousCMO;
    @CCD(
            label = "Orders will be sent for approval to:",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    String cmoJudgeInfo;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference cmoToSend;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement cmoToSendTranslationRequirements;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend0;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements0;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend1;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements1;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend2;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend3;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements3;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend4;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements4;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend5;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements5;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend6;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements6;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend7;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements7;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend8;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements8;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    DocumentReference orderToSend9;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    LanguageTranslationRequirement orderToSendTranslationRequirements9;
    @CCD(
            label = " ",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    String orderToSendOptionCount;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    YesNo showCMOsSentToJudge;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
    @Temp
    YesNo showReplacementCMO;
    @CCD(
            label = "Tick to confirm this document is related to this case",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DocumentAcknowledge",
            typeParameterClass = DocumentAcknowledge.class,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class}
    )
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

    @JsonIgnore
    public YesNo hasDraftOrderBeenUploadedThatNeedsApproval() {
        // if we've got a C21 draft order - these are always approved
        if (isNotEmpty(hearingOrderDraftKind) && hearingOrderDraftKind.contains(C21)) {
            return YesNo.YES;
        }
        // if we've got a CMO draft order - these are only approved if they're agreed CMOs
        if (isNotEmpty(cmoUploadType) && AGREED.equals(cmoUploadType)) {
            return YesNo.YES;
        }
        return YesNo.NO;
    }
}
