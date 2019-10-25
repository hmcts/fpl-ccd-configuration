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
public class CafcassEmailContentProvider extends AbstractEmailContentProvider {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final MapperService service;

    @Autowired
    public CafcassEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                                       CafcassLookupConfiguration cafcassLookupConfiguration,
                                       @Value("${ccd.ui.base.url}") String uiBaseUrl, MapperService service,
                                       DateFormatterService dateFormatterService,
                                       HearingBookingService hearingBookingService) {
        super(uiBaseUrl,dateFormatterService, hearingBookingService);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.service = service;
    }

    public Map<String, Object> buildCafcassSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        return super.getCasePersonalisationBuilder(caseDetails)
            .put("cafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildCafcassStandardDirectionOrderIssuedNotification(CaseDetails caseDetails, String localAuthorityCode) {
        CaseData caseData = service.mapObject(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails, caseData)
            .put("title", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .build();
    }
}
