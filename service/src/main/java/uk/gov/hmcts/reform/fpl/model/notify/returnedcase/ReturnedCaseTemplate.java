package uk.gov.hmcts.reform.fpl.model.notify.returnedcase;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;

@Getter
@Setter
public class ReturnedCaseTemplate implements NotifyData {
    private String familyManCaseNumber;
    private String returnedReasons;
    private String returnedNote;
    private String firstRespondentLastName;
    private String firstRespondentFullName;
    private String caseUrl;
    private String localAuthority;
}
