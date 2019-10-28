package uk.gov.hmcts.reform.fpl.service.email.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.MapperService;

import java.util.Map;

@Service
public class CafcassEmailContentProviderSDOIssued extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfig;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final MapperService service;

    @Autowired
    public CafcassEmailContentProviderSDOIssued(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfig,
                                                CafcassLookupConfiguration cafcassLookupConfiguration,
                                                @Value("${ccd.ui.base.url}") String uiBaseUrl, MapperService service,
                                                DateFormatterService dateFormatterService,
                                                HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService, hearingBookingService);
        this.localAuthorityNameLookupConfig = localAuthorityNameLookupConfig;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.service = service;
    }

    public Map<String, Object> buildCafcassStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                    String localAuthorityCode) {
        CaseData caseData = service.mapObject(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails, caseData)
            .put("title", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .build();
    }
}
