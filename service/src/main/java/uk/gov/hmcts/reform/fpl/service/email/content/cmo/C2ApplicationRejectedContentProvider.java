package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.C2ApplicationRejectedTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLine;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C2ApplicationRejectedContentProvider extends AbstractEmailContentProvider {

    public C2ApplicationRejectedTemplate buildContent(CaseData caseData, String refusalOrderTitle) {
        return C2ApplicationRejectedTemplate.builder()
            .caseUrl(getCaseUrl(caseData.getId(), TabUrlAnchor.ORDERS))
            .respondentLastName(getFirstRespondentLastName(caseData))
            .subjectLineWithHearingDate(
                buildSubjectLine(caseData.getFamilyManCaseNumber(), caseData.getAllRespondents()))
            .orderList(refusalOrderTitle)
            .build();
    }
}
