package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class C42FamilyAssistanceOrderDocmosisParameters extends DocmosisParameters {
    String partiesToBeBefriended;
    String furtherDirections;
    String orderEndDate;
    String orderByConsent;
    String orderDetails;
    String localAuthorityName;
    String noticeHeader;
    String noticeMessage;
}
