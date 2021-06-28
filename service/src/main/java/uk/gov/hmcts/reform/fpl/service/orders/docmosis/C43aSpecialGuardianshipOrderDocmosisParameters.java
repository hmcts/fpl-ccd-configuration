package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C43aSpecialGuardianshipOrderDocmosisParameters extends DocmosisParameters {
    String orderDetails;
    String furtherDirections;
    String orderByConsent;
    String orderHeader;
    String orderMessage;
    String noticeHeader;
    String noticeMessage;
}
