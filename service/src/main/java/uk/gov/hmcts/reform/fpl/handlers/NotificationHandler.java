package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.HmctsCourtLookUpService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class NotificationHandler {

    private final HmctsCourtLookUpService hmctsCourtLookUpService;
    private final LocalAuthorityService localAuthorityService;
    private final NotificationClient notificationClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String JURISDICTION = "PUBLICLAW";
    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";

    @Value("${ccd.ui.base.url}")
    private static String CCD_BASE_URL;

    @Autowired
    public NotificationHandler(HmctsCourtLookUpService hmctsCourtLookUpService,
                               NotificationClient notificationClient,
                               LocalAuthorityService localAuthorityService) {
        this.hmctsCourtLookUpService = hmctsCourtLookUpService;
        this.notificationClient = notificationClient;
        this.localAuthorityService = localAuthorityService;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = caseDetails.getData().get("caseLocalAuthority").toString();
        Map<String, String> parameters = buildEmailData(caseDetails, localAuthorityCode);
        String reference = caseDetails.getId().toString();
        String template = "1b1be684-9b0a-4e58-8e51-f0c3c2dba37c";


        String email = hmctsCourtLookUpService.getCourt(localAuthorityCode).getEmail();
        logger.debug("Sending email to {}", email);

        try {
            notificationClient.sendEmail(template, email, parameters, reference);
        } catch (NotificationClientException e) {
            logger.warn("Failed to send email to {}", email, e);
        }
    }

    private Map<String, String> buildEmailData(CaseDetails caseDetails, String localAuthorityCode) {
        LinkedHashMap orders =
            Optional.ofNullable((LinkedHashMap) caseDetails.getData().get("orders")).orElse(new LinkedHashMap());

        LinkedHashMap hearing =
            Optional.ofNullable((LinkedHashMap) caseDetails.getData().get("hearing")).orElse(new LinkedHashMap());

        String timeFramePresent;

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        } else {
            timeFramePresent = "No";
        }

        String orderType = Optional.ofNullable(orders.get("orderType"))
            .orElse("").toString();
        String directions = Optional.ofNullable(orders.get("directionsAndInterim")).orElse("").toString();
        String timeFrame = Optional.ofNullable(hearing.get("timeFrame")).orElse("").toString();
        String caseId = caseDetails.getId().toString();

        String courtName = hmctsCourtLookUpService.getCourt(localAuthorityCode).getName();
        String localAuthorityName = localAuthorityService.getLocalAuthorityName(localAuthorityCode);

        return ImmutableMap.<String, String>builder()
            .put("court", courtName)
            .put("localAuthority", localAuthorityName)
            .put("orders", orderType.replace("[", "").replace("]", ""))
            .put("directionsAndInterim", directions)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrame", timeFrame)
            .put("reference", caseId)
            .put("caseUrl", CCD_BASE_URL + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseId)
            .build();
    }
}
