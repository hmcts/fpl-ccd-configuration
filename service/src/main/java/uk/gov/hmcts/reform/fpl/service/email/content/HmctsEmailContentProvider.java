package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
    private final ObjectMapper mapper;

    public SubmitCaseHmctsTemplate buildHmctsSubmissionNotification(CaseDetails caseDetails,
                                                                    String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        SubmitCaseHmctsTemplate template = super.buildNotifyTemplate(new SubmitCaseHmctsTemplate(),
            caseDetails.getId(),
            caseData.getOrders(),
            caseData.getHearing(),
            caseData.getRespondents1());

        template.setCourt(hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName());
        template.setLocalAuthority(localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode));
        template.setDocumentLink(linkToAttachedDocument(caseData.getSubmittedForm()));

        return template;
    }
}
