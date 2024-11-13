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

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemovalToolData {

    RemovedApplicationForm hiddenApplicationForm;
    List<Element<HearingOrder>> hiddenCaseManagementOrders;
    List<Element<GeneratedOrder>> hiddenOrders;
    List<Element<StandardDirectionOrder>> hiddenStandardDirectionOrders;
    List<Element<StandardDirectionOrder>> hiddenUrgentDirectionOrders;
    List<Element<AdditionalApplicationsBundle>> hiddenApplicationsBundle;
    List<Element<SentDocuments>> hiddenDocumentsSentToParties;
    List<Element<RemovedPlacement>> removedPlacements;

    @Temp
    Object removableOrderList;

    @Temp
    Object removableApplicationList;

    @Temp
    Object removableSentDocumentList;

    @Temp
    RemovableType removableType;

    @Temp
    String applicationRemovalDetails;

    @Temp
    String reasonToRemoveApplicationForm;

    @Temp
    String reasonToRemoveOrder;

    @Temp
    String reasonToRemoveSentDocument;

    @Temp
    ApplicationRemovalReason reasonToRemoveApplication;

    @Temp
    Object removablePlacementApplicationList;

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
}
