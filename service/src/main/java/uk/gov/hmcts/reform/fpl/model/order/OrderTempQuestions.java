package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class OrderTempQuestions {

    String hearingDetails;
    String approver;
    String approvalDate;
    String approvalDateTime;
    String whichChildren;
    String dischargeOfCareDetails;
    String c43Details;
    String epoTypeAndPreventRemoval;
    String epoIncludePhrase;
    String epoChildrenDescription;
    String epoExpiryDate;
    String furtherDirections;
    String orderDetails;
    String isFinalOrder;
    String closeCase;
    String previewOrder;
}
