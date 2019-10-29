package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.HashMap;
import java.util.Map;

@Service
public class C21OrderEmailContentProvider extends C2UploadedEmailContentProvider {

    private final HearingBookingService hearingBookingService;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    protected C21OrderEmailContentProvider(@Value("${ccd.ui.base.url}")String uiBaseUrl,
                                           ObjectMapper objectMapper,
                                           HearingBookingService hearingBookingService,
                                           LocalAuthorityNameLookupConfiguration
                                               localAuthorityNameLookupConfiguration) {
        super(uiBaseUrl, objectMapper);
        this.hearingBookingService = hearingBookingService;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
    }

    public Map<String, Object> buildC21OrderNotification(final CaseDetails caseDetails,
                                                         final String localAuthorityCode) {
        // Validation within our frontend ensures that the following data is present
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        Map<String, Object> c21NotificationParams = new HashMap<>(super.buildC2UploadNotification(caseDetails));
        c21NotificationParams.put("localAuthorityOrCafcass",
            localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode));
        c21NotificationParams.put("hearingDate",
            hearingBookingService.getMostUrgentHearingBooking(caseData.getHearingDetails()));

        return c21NotificationParams;
    }
}
