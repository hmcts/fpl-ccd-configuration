package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
public class NotificationHandler {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final NotificationClient notificationClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String uiBaseUrl;

    @Autowired
    public NotificationHandler(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                               LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                               NotificationClient notificationClient,
                               @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.notificationClient = notificationClient;
        this.uiBaseUrl = uiBaseUrl;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = caseDetails.getData().get("caseLocalAuthority").toString();
        Map<String, String> parameters = buildEmailData(caseDetails, localAuthorityCode);
        String reference = caseDetails.getId().toString();

        String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();
        logger.debug(
            "Sending submission notification (with template id: {}) to {}", HMCTS_COURT_SUBMISSION_TEMPLATE, email);

        try {
            notificationClient.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.warn("Failed to send submission notification (with template id: {}) to {}",
                HMCTS_COURT_SUBMISSION_TEMPLATE, email, e);
        }
    }

    private Map<String, String> buildEmailData(CaseDetails caseDetails, String localAuthorityCode) {
        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get("orders")).orElse(ImmutableMap.builder().build());

        ArrayList orderType = (ArrayList) Optional.ofNullable(orders.get("orderType")).orElse(new ArrayList<>());

        String ordersKey = "orders";

        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ordersKey + i, (String) orderType.get(i));
            } else {
                orderTypeArray.put(ordersKey + i, "");
            }
        }

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        return ImmutableMap.<String, String>builder()
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .putAll(orderTypeArray.build())
            .put("directionsAndInterim", Optional.ofNullable((String) orders.get("directionsAndInterim"))
                .orElse(""))
            .put("timeFramePresent", (hearing.containsKey("timeFrame")) ? ("Yes") : ("No"))
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId())
            .build();
    }
}
