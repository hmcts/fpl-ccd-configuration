package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartyAddedNotifyData implements NotifyData {
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String reference;
    private final String caseUrl;
    private final String childLastName;
}
