package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C43ChildArrangementOrderDocmosisParameters extends DocmosisParameters {
    String orderHeader;
    String orderMessage;
    String orderByConsent;
    String recitalsOrPreamble;
    String prohibitedStepsOrderDetails;
    String specificIssueOrderDetails;
    String childArrangementsContactWithDetails;
    String childArrangementsLiveWithDetails;
    String orderDetails;
    String furtherDirections;
    String localAuthorityName;
    String noticeHeader;
    String noticeMessage;
}
