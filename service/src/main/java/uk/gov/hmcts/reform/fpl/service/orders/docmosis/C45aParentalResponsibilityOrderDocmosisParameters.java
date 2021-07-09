package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C45aParentalResponsibilityOrderDocmosisParameters extends DocmosisParameters {
    String orderDetails;
    String furtherDirections;
    String orderByConsent;
    String noticeHeader;
    String noticeMessage;
}
