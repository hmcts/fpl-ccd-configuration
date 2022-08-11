package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.ApplicationRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.RemovableType;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemovalToolData {

    Object removableOrderList;
    Object removableApplicationList;
    String reasonToRemoveOrder;
    List<Element<GeneratedOrder>> hiddenOrders;
    RemovableType removableType;
    ApplicationRemovalReason reasonToRemoveApplication;
    String applicationRemovalDetails;
    String reasonToRemoveApplicationForm;
    DocumentReference hiddenApplicationForm;
    List<Element<HearingOrder>> hiddenCaseManagementOrders;
    List<Element<StandardDirectionOrder>> hiddenStandardDirectionOrders;
    List<Element<AdditionalApplicationsBundle>> hiddenApplicationsBundle;

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

}
