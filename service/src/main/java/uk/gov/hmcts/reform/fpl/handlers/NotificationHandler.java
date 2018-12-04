package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.EmailLookUpService;
import uk.gov.hmcts.reform.fpl.service.NotificationService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Component
public class NotificationHandler {

    private final EmailLookUpService emailLookUpService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationHandler(EmailLookUpService emailLookUpService, NotificationService notificationService) {
        this.emailLookUpService = emailLookUpService;
        this.notificationService = notificationService;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = caseDetails.getData().get("caseLocalAuthority").toString();
        Map<String, String> parameters = buildEmailData(caseDetails);
        String reference = caseDetails.getId().toString();
        String template = "1b1be684-9b0a-4e58-8e51-f0c3c2dba37c";

        emailLookUpService.getEmails(localAuthorityCode)
            .forEach(email -> {
                try {
                    notificationService.sendMail(email, template, parameters, reference);
                } catch (NotificationClientException e) {
                    e.printStackTrace();
                }
            });
    }

    private Map<String, String> buildEmailData(CaseDetails caseDetails) {
        return ImmutableMap.<String, String>builder()
            .put("court", "")
            .put("localAuthority", "")
            .put("orders", "")
            .put("directionsAndInterim", "")
            .put("timeFrame", "")
            .put("reference", caseDetails.getId().toString())
            .put("caseUrl", "")
            .build();
    }
}
