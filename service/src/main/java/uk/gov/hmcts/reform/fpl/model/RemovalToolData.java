package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ApplicationRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.RemovableType;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getFieldsListWithAnnotation;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemovalToolData {

    @CCD(
            label = "Removed Application Form",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    RemovedApplicationForm hiddenApplicationForm;
    @CCD(
            label = "Removed case management orders",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawSuperuserCuAccess.class}
    )
    List<Element<HearingOrder>> hiddenCaseManagementOrders;
    @CCD(label = "Other removed orders", searchable = false, access = {CaseworkerPubliclawSuperuserCudAccess.class})
    List<Element<GeneratedOrder>> hiddenOrders;
    @CCD(
            label = "Removed gatekeeping orders",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCuAccess.class}
    )
    List<Element<StandardDirectionOrder>> hiddenStandardDirectionOrders;
    @CCD(
            label = "Removed urgent directions orders",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCuAccess.class}
    )
    List<Element<StandardDirectionOrder>> hiddenUrgentDirectionOrders;
    @CCD(label = "Removed applications", searchable = false, access = {CaseworkerPubliclawSuperuserCudAccess.class})
    List<Element<AdditionalApplicationsBundle>> hiddenApplicationsBundle;
    @CCD(
            label = "Removed documents sent to parties",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsSentToParty",
            access = {CaseworkerPubliclawSuperuserCuAccess.class}
    )
    List<Element<SentDocuments>> hiddenDocumentsSentToParties;
    @CCD(
            label = "Removed placement application",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCudAccess.class}
    )
    List<Element<RemovedPlacement>> removedPlacements;

    @CCD(
            label = "Choose the order you want to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    Object removableOrderList;

    @CCD(
            label = "Choose the application you want to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    Object removableApplicationList;

    @CCD(
            label = "Choose the document sent to parties you want to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    Object removableSentDocumentList;

    @CCD(
            label = "What do you want to remove?",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    RemovableType removableType;

    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    String applicationRemovalDetails;

    @CCD(
            label = "Why is the application form being removed?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    String reasonToRemoveApplicationForm;

    @CCD(
            label = "Why is the order being removed?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    String reasonToRemoveOrder;

    @CCD(
            label = "Why is the document being removed?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    String reasonToRemoveSentDocument;

    @CCD(
            label = "Why is the application being removed?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ApplicationRemovalReason",
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    ApplicationRemovalReason reasonToRemoveApplication;

    @CCD(
            label = "Choose the placement application you want to remove",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    Object removablePlacementApplicationList;

    @CCD(
            label = "Why is the placement application being removed?",
            searchable = false,
            access = {CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    @Temp
    String reasonToRemovePlacementApplication;

    static List<String> otherTemporaryFields = List.of("orderTitleToBeRemoved", "applicationTypeToBeRemoved",
        "orderToBeRemoved", "c2ApplicationToBeRemoved", "otherApplicationToBeRemoved", "orderIssuedDateToBeRemoved",
        "orderDateToBeRemoved", "hearingToUnlink", "showRemoveCMOFieldsFlag", "showRemoveSDOWarningFlag",
        "showReasonFieldFlag", "partyNameToBeRemoved", "sentAtToBeRemoved", "letterIdToBeRemoved",
        "sentDocumentToBeRemoved", "removablePlacementApplicationList", "placementApplicationToBeRemoved",
        "reasonToRemovePlacementApplication");

    public static List<String> temporaryFields() {
        List<String> tempFields = getFieldsListWithAnnotation(RemovalToolData.class, Temp.class).stream()
            .map(Field::getName)
            .collect(toList());
        tempFields.addAll(otherTemporaryFields);
        return tempFields;
    }

    public List<Element<SentDocuments>> getHiddenDocumentsSentToParties() {
        return defaultIfNull(hiddenDocumentsSentToParties, new ArrayList<>());
    }

    public List<Element<AdditionalApplicationsBundle>> getHiddenApplicationsBundle() {
        return defaultIfNull(hiddenApplicationsBundle, new ArrayList<>());
    }

    public List<Element<StandardDirectionOrder>> getHiddenStandardDirectionOrders() {
        return defaultIfNull(hiddenStandardDirectionOrders, new ArrayList<>());
    }

    public List<Element<HearingOrder>> getHiddenCMOs() {
        return defaultIfNull(hiddenCaseManagementOrders, new ArrayList<>());
    }

    public List<Element<GeneratedOrder>> getHiddenOrders() {
        return defaultIfNull(hiddenOrders, new ArrayList<>());
    }

    public List<Element<RemovedPlacement>> getRemovedPlacements() {
        return defaultIfNull(removedPlacements, new ArrayList<>());
    }

    public  List<Element<StandardDirectionOrder>> getHiddenUrgentDirectionOrders() {
        return defaultIfNull(hiddenUrgentDirectionOrders, new ArrayList<>());
    }

}
