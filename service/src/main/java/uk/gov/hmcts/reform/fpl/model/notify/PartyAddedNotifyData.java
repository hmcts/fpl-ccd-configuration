package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class PartyAddedNotifyData implements NotifyData {
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String reference;
    private final String caseUrl;
}
