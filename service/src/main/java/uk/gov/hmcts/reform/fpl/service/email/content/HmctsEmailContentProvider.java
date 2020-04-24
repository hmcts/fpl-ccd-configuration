package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.SubmitCaseHmctsTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.NotifyTemplateContentProvider;

@Service
public class HmctsEmailContentProvider extends NotifyTemplateContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    protected HmctsEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                        ObjectMapper mapper,
                                        LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                        HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration) {
        super(uiBaseUrl, mapper);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
    }

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

        return template;
    }
}
