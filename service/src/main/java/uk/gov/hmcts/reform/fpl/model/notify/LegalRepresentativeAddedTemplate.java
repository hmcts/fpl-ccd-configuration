package uk.gov.hmcts.reform.fpl.model.notify;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LegalRepresentativeAddedTemplate implements NotifyData {
    private final String repName;
    private final String localAuthority;
    private final String firstRespondentLastName;
    private final String familyManCaseNumber;
    private final String caseUrl;
    private final String childLastName;
}
