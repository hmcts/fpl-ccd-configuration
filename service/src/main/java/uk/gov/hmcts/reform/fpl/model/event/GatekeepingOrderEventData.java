package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CustomDirection;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.StandardDirection;
import uk.gov.hmcts.reform.fpl.model.Temp;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCrudCaseworkerPubliclawJudiciaryCruAccess;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatekeepingOrderEventData {

    @CCD(
            label = "Your order",
            regex = ".doc,.docx,.pdf",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    @Temp
    DocumentReference urgentHearingOrderDocument;
    @CCD(
            label = "Allocation decision",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    @Temp
    Allocation urgentHearingAllocation;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    @Temp
    YesNo showUrgentHearingAllocation;

    @CCD(
            label = "Confirm or edit issuing judge",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    JudgeAndLegalAdvisor gatekeepingOrderIssuingJudge;
    @CCD(
            label = "Check, save or send the order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    GatekeepingOrderSealDecision gatekeepingOrderSealDecision;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForAllParties",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForAllParties;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForCourt",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForCourt;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForCourtUpdated",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForCourtUpdated;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForLocalAuthority",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForLocalAuthority;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForCafcass",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForCafcass;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForCafcassUpdated",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForCafcassUpdated;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForRespondents",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForRespondents;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DirectionTypesForOthers",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> directionsForOthers;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "UrgentDirectionTypesForAllParties",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> urgentDirectionsForAllParties;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "UrgentDirectionTypesForLocalAuthority",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> urgentDirectionsForLocalAuthority;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "UrgentDirectionTypesForCafcass",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    List<DirectionType> urgentDirectionsForCafcass;


    @CCD(
            label = "Further directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    List<Element<CustomDirection>> customDirections;
    @CCD(
            label = "Standard directions",
            searchable = false,
            access = {CaseworkerPubliclawGatekeeperCaseworkerPubliclawJudiciaryCrudAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    List<Element<StandardDirection>> standardDirections;

    @CCD(
            label = "Local court admin will be notified by email that they need to list and serve this order. The order will be saved in the \"Draft orders tab\" until served.",
            hint = "Enter any listing or allocated judge instructions for local court admin",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    String gatekeepingOrderListOrSendToAdminReason;
    @CCD(
            label = "What is your local agreement for listing and serving the order?",
            hint = "If you need an urgent listing you should complete the listing and serve the order yourself to avoid any delay",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "GatekeepingListOrSendToAdmin",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    String gatekeepingOrderListOrSendToAdmin;

    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    LanguageTranslationRequirement gatekeepingTranslationRequirements;
    @CCD(
            label = "Is translation needed?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    LanguageTranslationRequirement urgentGatekeepingTranslationRequirements;

    @CCD(
            label = "Send this order",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawSuperuserCruAccess.class}
    )
    DocumentReference currentSDO;
    @CCD(
            label = "Used in allowing progression through SDO event for the second time with the service route",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class, CaseworkerPubliclawGatekeeperCrudCaseworkerPubliclawJudiciaryCruAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    YesNo useUploadRoute;
    @CCD(
            label = "Used in allowing progression through SDO event for the second time with the service route",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerPubliclawCourtadminCrudPlus1RolesUzkhikAccess.class, CaseworkerPubliclawGatekeeperCrudCaseworkerPubliclawJudiciaryCruAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    YesNo useServiceRoute;

    @JsonIgnore
    public List<DirectionType> getRequestedDirections() {
        return Stream.of(urgentDirectionsForAllParties, urgentDirectionsForLocalAuthority, urgentDirectionsForCafcass,
                directionsForAllParties, directionsForLocalAuthority, directionsForCafcass, directionsForCourt,
                directionsForCafcassUpdated, directionsForCourtUpdated, directionsForRespondents, directionsForOthers)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(toList());
    }

    public JudgeAndLegalAdvisor getGatekeepingOrderIssuingJudge() {
        return defaultIfNull(gatekeepingOrderIssuingJudge, JudgeAndLegalAdvisor.builder().build());
    }

    public GatekeepingOrderSealDecision getGatekeepingOrderSealDecision() {
        return defaultIfNull(gatekeepingOrderSealDecision, GatekeepingOrderSealDecision.builder().build());
    }

    public static List<String> temporaryFields() {
        return getFieldsListWithAnnotation(GatekeepingOrderEventData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(toList());
    }

    public List<Element<StandardDirection>> resetStandardDirections() {
        this.standardDirections = new ArrayList<>();
        return standardDirections;
    }

    @JsonIgnore
    public boolean isSentToAdmin() {
        return Optional.ofNullable(gatekeepingOrderListOrSendToAdmin)
            .map(value -> value.equals("NO"))
            .orElse(false);
    }

    @JsonIgnore
    public String getSendToAdminReason() {
        return gatekeepingOrderListOrSendToAdminReason;
    }
}
