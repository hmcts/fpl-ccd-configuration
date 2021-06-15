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
    String whichChildren;
    String epoTypeAndPreventRemoval;
    String epoIncludePhrase;
    String epoChildrenDescription;
    String epoExpiryDate;
    String furtherDirections;
    String orderDetails;
    String closeCase;
    String previewOrder;
    String manageOrdersExpiryDateWithMonth;
    String manageOrdersExclusionRequirementDetails;
    String cafcassJurisdictions;
    String manageOrdersExpiryDateWithEndOfProceedings;

}
