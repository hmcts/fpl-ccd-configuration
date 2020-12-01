package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseCafcassTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.SharedNotifyContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassEmailContentProvider extends SharedNotifyContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    public SubmitCaseCafcassTemplate buildCafcassSubmissionNotification(CaseData caseData) {

        SubmitCaseCafcassTemplate template = buildNotifyTemplate(SubmitCaseCafcassTemplate.builder().build(),
            caseData.getId(), caseData.getOrders(), caseData.getHearing(), caseData.getRespondents1());

        template.setCafcass(cafcassLookupConfiguration.getCafcass(caseData.getCaseLocalAuthority()).getName());
        template.setLocalAuthority(localAuthorityNameLookupConfiguration
            .getLocalAuthorityName(caseData.getCaseLocalAuthority()));
        template.setDocumentLink(linkToAttachedDocument(caseData.getSubmittedForm()));

        return template;
    }
}
