package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Map;

@Service
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final MapperService service;

    @Autowired
    public LocalAuthorityEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfig,
                                              HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                              @Value("${ccd.ui.base.url}") String uiBaseUrl, MapperService service) {
        super(uiBaseUrl);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfig;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.service = service;
    }

    public Map<String, Object> buildLocalAuthoritySDOSubmissionNotification(CaseDetails caseDetails,
                                                                            String localAuthorityCode) {
        CaseData caseData = service.mapObject(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails, caseData)
            .put("cafcass", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
