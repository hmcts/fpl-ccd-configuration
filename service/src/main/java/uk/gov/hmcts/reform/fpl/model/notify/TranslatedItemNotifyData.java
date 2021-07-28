package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Value;


@Builder(toBuilder = true)
@Value
public class TranslatedItemNotifyData implements NotifyData, ModifiedItemNotifyData {
    String childLastName;
    String docType;
    String courtName;
    String callout;
    String caseUrl;
}
