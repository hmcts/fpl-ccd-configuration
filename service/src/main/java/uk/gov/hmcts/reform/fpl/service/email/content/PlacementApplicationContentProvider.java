package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class PlacementApplicationContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;

    @Autowired
    public PlacementApplicationContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              ObjectMapper mapper,
                                              HearingBookingService hearingBookingService) {
        super(uiBaseUrl, hearingBookingService);
        this.mapper = mapper;
    }

    public Map<String, Object> buildPlacementApplicationNotificationParameters(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.of(
            "respondentLastName", getFirstRespondentLastName(caseData.getRespondents1()),
            "caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId())
        );
    }
}
