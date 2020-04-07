package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.StandardDirectionOrderContent;

import java.util.Map;

@Service
public class CafcassEmailContentProviderSDOIssued extends StandardDirectionOrderContent {
    private final CafcassLookupConfiguration config;

    @Autowired
    protected CafcassEmailContentProviderSDOIssued(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                                   ObjectMapper mapper,
                                                   HearingBookingService hearingBookingService,
                                                   CafcassLookupConfiguration config) {
        super(uiBaseUrl, mapper, hearingBookingService);
        this.config = config;
    }

    public Map<String, Object> buildCafcassStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                    String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails.getId(), caseData)
            .put("title", config.getCafcass(localAuthorityCode).getName())
            .build();
    }
}
