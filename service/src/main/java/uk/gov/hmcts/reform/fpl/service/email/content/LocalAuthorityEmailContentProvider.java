package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {
    private final LocalAuthorityNameLookupConfiguration config;

    @Autowired
    protected LocalAuthorityEmailContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              LocalAuthorityNameLookupConfiguration config) {
        super(uiBaseUrl);
        this.config = config;
    }

    public Map<String, Object> buildLocalAuthorityStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                           String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails.getId(), caseData)
            .put("title", config.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildNoticeOfPlacementOrderUploadedNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return Map.of(
            "respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId()));
    }
}
