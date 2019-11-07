package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

@Service
public class GatekeeperEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    public GatekeeperEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                          @Value("${ccd.ui.base.url}") String uiBaseUrl,
                                          DateFormatterService dateFormatterService,
                                          HearingBookingService hearingBookingService) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    public Map<String, Object> buildGatekeeperNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
