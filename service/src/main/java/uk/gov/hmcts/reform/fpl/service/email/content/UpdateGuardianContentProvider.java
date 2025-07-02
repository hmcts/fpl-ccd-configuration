package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.UpdateGuardianNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class UpdateGuardianContentProvider extends AbstractEmailContentProvider {

    public UpdateGuardianNotifyData getUpdateGuardianNotifyData(CaseData caseData) {
        return UpdateGuardianNotifyData.builder()
            .firstRespondentLastName(getFirstRespondentLastName(caseData))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .caseUrl(getCaseUrl(caseData.getId()))
            .build();
    }
}
