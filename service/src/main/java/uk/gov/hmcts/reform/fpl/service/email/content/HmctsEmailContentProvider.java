package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HmctsEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final CourtService courtService;

    public SubmitCaseHmctsTemplate buildHmctsSubmissionNotification(CaseData caseData) {
        SubmitCaseHmctsTemplate template = buildNotifyTemplate(SubmitCaseHmctsTemplate.builder().build(), caseData);

        template.setCourt(courtService.getCourtName(caseData));
        template.setLocalAuthority(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        template.setDocumentLink(getDocumentUrl(caseData.getC110A().getSubmittedForm()));

        return template;
    }
}
