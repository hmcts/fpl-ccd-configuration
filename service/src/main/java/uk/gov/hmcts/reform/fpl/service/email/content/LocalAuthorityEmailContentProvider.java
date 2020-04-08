package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class LocalAuthorityEmailContentProvider extends AbstractEmailContentProvider {
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final ObjectMapper mapper;

    @Autowired
    public LocalAuthorityEmailContentProvider(LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfig,
                                              @Value("${ccd.ui.base.url}") String uiBaseUrl, ObjectMapper mapper,
                                              HearingBookingService hearingBookingService) {
        super(uiBaseUrl, hearingBookingService);
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfig;
        this.mapper = mapper;
    }

    public Map<String, Object> buildLocalAuthorityStandardDirectionOrderIssuedNotification(CaseDetails caseDetails,
                                                                                           String localAuthorityCode) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return super.getSDOPersonalisationBuilder(caseDetails.getId(), caseData)
            .put("title", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .build();
    }

    public Map<String, Object> buildNoticeOfPlacementOrderUploadedNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return Map.of(
            "respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId()));
    }
}
