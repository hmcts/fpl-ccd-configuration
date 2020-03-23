package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
public class PartyAddedToCaseContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper objectMapper;

    @Autowired
    public PartyAddedToCaseContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                           DateFormatterService dateFormatterService,
                                           HearingBookingService hearingBookingService,
                                           ObjectMapper objectMapper) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getPartyAddedToCaseNotificationParameters(CaseDetails caseDetails,
        RepresentativeServingPreferences servingPreference) {
        ImmutableMap.Builder<String, Object> notificationParams =
            buildPartyAddedToCaseCommonNotificationParams(caseDetails);

        if (servingPreference == DIGITAL_SERVICE) {
            notificationParams.put("caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId()));
        }
        return notificationParams.build();
    }

    private ImmutableMap.Builder<String, Object> buildPartyAddedToCaseCommonNotificationParams(
        final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), ""));
    }
}
