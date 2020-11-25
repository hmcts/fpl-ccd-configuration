package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class LegalRepresentativeAddedTemplate implements NotifyData {
    private final String repName;
    private final String localAuthority;
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String caseUrl;
}
