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
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
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
                                        RepresentativeServingPreferences servingPreferences) {
        if (servingPreferences == EMAIL) {
            return buildPartyAddedToCaseCommonNotificationParams(caseDetails).build();
        } else {
            ImmutableMap.Builder<String, Object> notificationParameters =
                buildPartyAddedToCaseCommonNotificationParams(caseDetails);
            notificationParameters.put("caseUrl", formatCaseUrl(uiBaseUrl, caseDetails.getId()));
            return notificationParameters.build();
        }
    }

    public String getPartyAddedToCaseNotificationTemplate(RepresentativeServingPreferences servingPreferences) {
        if (servingPreferences == EMAIL) {
            return PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
        }     else {
            return PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
        }
    }

    private ImmutableMap.Builder<String, Object> buildPartyAddedToCaseCommonNotificationParams(
        final CaseDetails caseDetails) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

        return ImmutableMap.<String, Object>builder()
            .put("firstRespondentLastName", getFirstRespondentLastName(caseData.getRespondents1()))
            .put("familyManCaseNumber", defaultIfNull(caseData.getFamilyManCaseNumber(), ""));
    }
}
