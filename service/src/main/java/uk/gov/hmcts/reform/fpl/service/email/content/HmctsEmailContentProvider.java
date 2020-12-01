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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HmctsEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    public SubmitCaseHmctsTemplate buildHmctsSubmissionNotification(CaseData caseData) {

        SubmitCaseHmctsTemplate template = super.buildNotifyTemplate(SubmitCaseHmctsTemplate.builder().build(),
            caseData.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setCourt(hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName());
        template.setLocalAuthority(localAuthorityNameLookupConfiguration
            .getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        template.setDocumentLink(linkToAttachedDocument(caseData.getSubmittedForm()));

        return template;
    }
}
