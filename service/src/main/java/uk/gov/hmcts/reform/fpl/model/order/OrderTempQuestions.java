package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class OrderTempQuestions {

    String hearingDetails;
    String linkApplication;
    String approver;
    String approvalDate;
    String approvalDateTime;
    String cafcassJurisdictions;
    String whichChildren;
    String dischargeOfCareDetails;
    String epoTypeAndPreventRemoval;
    String epoIncludePhrase;
    String epoChildrenDescription;
    String epoExpiryDate;
    String furtherDirections;
    String orderDetails;
    String isFinalOrder;
    String manageOrdersExpiryDateWithMonth;
    String manageOrdersExpiryDateWithEndOfProceedings;
    String manageOrdersExclusionRequirementDetails;
    String needSealing;
    String closeCase;
    String uploadOrderFile;
    String previewOrder;

}
