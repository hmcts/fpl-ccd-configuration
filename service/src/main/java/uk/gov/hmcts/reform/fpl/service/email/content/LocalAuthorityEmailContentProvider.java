package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

@Service
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final ObjectMapper service;

    @Autowired
    public LocalAuthorityEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfig,
                                              HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                                              @Value("${ccd.ui.base.url}") String uiBaseUrl, ObjectMapper service,
                                              DateFormatterService dateFormatterService,
                                              HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService,hearingBookingService);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfig;
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.service = service;
    }

    public Map<String, Object> buildLocalAuthorityStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                           String localAuthorityCode) {
        CaseData caseData = service.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails, caseData)
            .put("title", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }
}
