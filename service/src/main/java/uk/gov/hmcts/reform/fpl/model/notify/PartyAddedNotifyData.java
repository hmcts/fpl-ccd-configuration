package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@EqualsAndHashCode
@SuperBuilder
public class PartyAddedNotifyData implements NotifyData {
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String reference;
    private final String caseUrl;
}
