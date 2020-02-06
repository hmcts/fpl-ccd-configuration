package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

@Service
public class PartyAddedToCaseContentProvider extends AbstractEmailContentProvider {

    private final PartyAddedToCaseByEmailContentProvider partyAddedToCaseEmailContentProvider;
    private final PartyAddedToCaseThroughDigitalServicelContentProvider partyAddedToCaseThroughDigitalServicelContentProvider;

    @Autowired
    protected PartyAddedToCaseContentProvider(@Value("${ccd.ui.base.url}") String uiBaseUrl,
                                              DateFormatterService dateFormatterService,
                                              HearingBookingService hearingBookingService,
                                              PartyAddedToCaseByEmailContentProvider partyAddedToCaseEmailContentProvider,
                                              PartyAddedToCaseThroughDigitalServicelContentProvider partyAddedToCaseThroughDigitalServicelContentProvider) {
        super(uiBaseUrl, dateFormatterService, hearingBookingService);
        this.partyAddedToCaseEmailContentProvider = partyAddedToCaseEmailContentProvider;
        this.partyAddedToCaseThroughDigitalServicelContentProvider = partyAddedToCaseThroughDigitalServicelContentProvider;
    }

    public Map<String, Object> getPartyAddedToCaseNotificationParameters(CaseDetails caseDetails, RepresentativeServingPreferences servingPreferences) {
        if (servingPreferences.equals(EMAIL)) {
            return partyAddedToCaseEmailContentProvider
                .buildPartyAddedToCaseNotification(caseDetails);
        } else return partyAddedToCaseThroughDigitalServicelContentProvider
            .buildPartyAddedToCaseNotification(caseDetails);
    }

    public String getPartyAddedToCaseNotificationTemplate(RepresentativeServingPreferences servingPreferences) {
        if (servingPreferences.equals(EMAIL)) {
            return PARTY_ADDED_TO_CASE_BY_EMAIL_NOTIFICATION_TEMPLATE;
        } else return PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_NOTIFICATION_TEMPLATE;
    }
}
