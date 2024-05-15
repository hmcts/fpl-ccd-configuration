package uk.gov.hmcts.reform.fpl.service.orders.docmosis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Value
@SuperBuilder(toBuilder = true)
public class TransparencyOrderDocmosisParameters extends DocmosisParameters {
    String orderByConsent;
    String orderExpiration;
    String publishInformationDetails;
    String publishIdentityDetails;
    String publishDocumentsDetails;
    String permissionToReportEffectiveDate;
}
