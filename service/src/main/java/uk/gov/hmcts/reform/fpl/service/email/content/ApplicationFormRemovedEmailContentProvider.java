package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.ApplicationFormRemovedNotifyData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ApplicationFormRemovedEmailContentProvider extends AbstractEmailContentProvider {

    public ApplicationFormRemovedNotifyData getNotifyData(final CaseData caseData) {
        return ApplicationFormRemovedNotifyData.builder()
            .caseName(caseData.getCaseName())
            .removalReason(caseData.getRemovalToolData().getHiddenApplicationForm().getRemovalReason())
            .familyManCaseNumber(!isEmpty(caseData.getFamilyManCaseNumber()) ? caseData.getFamilyManCaseNumber() : "")
            .caseUrl(getCaseUrl(caseData.getId()))
            .lastName(getFirstRespondentLastName(caseData))
            .build();
    }
}
