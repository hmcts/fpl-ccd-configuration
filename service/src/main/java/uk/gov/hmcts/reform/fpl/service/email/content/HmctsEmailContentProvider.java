package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HmctsEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration laNameLookup;
    private final HmctsCourtLookupConfiguration hmctsLookup;

    public SubmitCaseHmctsTemplate buildHmctsSubmissionNotification(CaseData caseData) {
        SubmitCaseHmctsTemplate template = buildNotifyTemplate(SubmitCaseHmctsTemplate.builder().build(), caseData);

        template.setCourt(hmctsLookup.getCourt(caseData.getCaseLocalAuthority()).getName());
        template.setLocalAuthority(laNameLookup.getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        template.setDocumentLink(getDocumentUrl(caseData.getSubmittedForm()));

        return template;
    }
}
