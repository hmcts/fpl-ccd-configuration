package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
public class NotificationHandler {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassEmailLookupConfiguration cafcassEmailLookupConfiguration;
    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final NotificationClient notificationClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String uiBaseUrl;

    @Autowired
    public NotificationHandler(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                               CafcassEmailLookupConfiguration cafcassEmailLookupConfiguration,
                               LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration,
                               NotificationClient notificationClient,
                               @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.cafcassEmailLookupConfiguration = cafcassEmailLookupConfiguration;
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.notificationClient = notificationClient;
        this.uiBaseUrl = uiBaseUrl;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get("caseLocalAuthority");
        Map<String, String> parameters = buildHmctsSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();

        logger.debug(
            "Sending submission notification (with template id: {}) to {}", HMCTS_COURT_SUBMISSION_TEMPLATE, email);

        try {
            notificationClient.sendEmail(HMCTS_COURT_SUBMISSION_TEMPLATE, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.error("Failed to send submission notification (with template id: {}) to {}",
                HMCTS_COURT_SUBMISSION_TEMPLATE, email, e);
        }
    }

    @EventListener
    public void sendNotificationToCafcass(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get("caseLocalAuthority");
        Map<String, String> parameters = buildCafcassSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = (String.valueOf(caseDetails.getId()));
        String email = cafcassEmailLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        logger.debug(
            "Sending submission notification (with template id: {}) to {}", CAFCASS_SUBMISSION_TEMPLATE, email);
        try {
            notificationClient.sendEmail(CAFCASS_SUBMISSION_TEMPLATE, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.error("Failed to send submission notification (with template id: {}) to {}",
                CAFCASS_SUBMISSION_TEMPLATE, email, e);
        }
    }

    private Map<String, String> buildHmctsSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        String ordersKey = "orders";
        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get(ordersKey)).orElse(ImmutableMap.builder().build());

        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());

        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ordersKey + i, "^" + orderType.get(i));
            } else {
                orderTypeArray.put(ordersKey + i, "");
            }
        }

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        String directionsAndInterimKey = "directionsAndInterim";

        return ImmutableMap.<String, String>builder()
            .put("court", hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .put("dataPresent", (orderType.isEmpty()) ? ("No") : ("Yes"))
            .put("fullStop", (orderType.isEmpty()) ? ("Yes") : ("No"))
            .putAll(orderTypeArray.build())
            .put(directionsAndInterimKey, (orders.containsKey(directionsAndInterimKey))
                ? ("^" + orders.get(directionsAndInterimKey)) : (""))
            .put("timeFramePresent", (hearing.containsKey("timeFrame")) ? ("Yes") : ("No"))
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId())
            .build();
    }

    private Map<String, String> buildCafcassSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        final String name = cafcassEmailLookupConfiguration.getCafcass(localAuthorityCode).getName();
        String ordersKey = "orders";
        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get(ordersKey)).orElse(ImmutableMap.builder().build());

        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());

        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ordersKey + i, "^" + orderType.get(i));
            } else {
                orderTypeArray.put(ordersKey + i, "");
            }
        }

        return ImmutableMap.<String, String>builder()
            .put("cafcass", name)
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .putAll(orderTypeArray.build())
            .put("dataPresent", (orderType.isEmpty()) ? ("No") : ("Yes"))
            .put("fullStop", (orderType.isEmpty()) ? ("Yes") : ("No"))
            .put("directionsAndInterim", (orders.containsKey("directionsAndInterim"))
                ? "^" + (orders.get("directionsAndInterim")) : "")
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId())
            .build();
    }
}
