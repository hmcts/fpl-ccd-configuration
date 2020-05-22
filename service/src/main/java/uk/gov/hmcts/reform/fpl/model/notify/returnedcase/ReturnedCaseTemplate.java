package uk.gov.hmcts.reform.fpl.model.notify.returnedcase;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.notify.SharedNotifyTemplate;

@Getter
@Setter
public class ReturnedCaseTemplate extends SharedNotifyTemplate {
    private String familyManCaseNumber;
    private String returnedReasons;
    private String returnedNote;
    private String firstRespondentLastName;
}
