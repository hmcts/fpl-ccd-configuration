package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.events.C21OrderEvent;
import uk.gov.hmcts.reform.fpl.events.C2UploadedEvent;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.events.SubmittedCaseEvent;
import uk.gov.hmcts.reform.fpl.service.EmailNotificationService;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.email.content.C21OrderEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.C2UploadedEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.GatekeeperEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.email.content.HmctsEmailContentProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.C21_ORDER_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.C2_UPLOAD_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.CAFCASS_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.NotificationTemplateType.HMCTS_COURT_SUBMISSION_TEMPLATE;

@Component
public class NotificationHandler {

    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final HmctsEmailContentProvider hmctsEmailContentProvider;
    private final CafcassEmailContentProvider cafcassEmailContentProvider;
    private final GatekeeperEmailContentProvider gatekeeperEmailContentProvider;
    private final C2UploadedEmailContentProvider c2UploadedEmailContentProvider;
    private final EmailNotificationService emailNotificationService;
    private final UserDetailsService userDetailsService;
    private final C21OrderEmailContentProvider c21OrderEmailContentProvider;

    @Autowired
    public NotificationHandler(HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration,
                               CafcassLookupConfiguration cafcassLookupConfiguration,
                               HmctsEmailContentProvider hmctsEmailContentProvider,
                               CafcassEmailContentProvider cafcassEmailContentProvider,
                               GatekeeperEmailContentProvider gatekeeperEmailContentProvider,
                               C2UploadedEmailContentProvider c2UploadedEmailContentProvider,
                               UserDetailsService userDetailsService,
                               EmailNotificationService emailNotificationService,
                               C21OrderEmailContentProvider c21OrderEmailContentProvider) {
        this.hmctsCourtLookupConfiguration = hmctsCourtLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.hmctsEmailContentProvider = hmctsEmailContentProvider;
        this.cafcassEmailContentProvider = cafcassEmailContentProvider;
        this.gatekeeperEmailContentProvider = gatekeeperEmailContentProvider;
        this.emailNotificationService = emailNotificationService;
        this.c2UploadedEmailContentProvider = c2UploadedEmailContentProvider;
        this.userDetailsService = userDetailsService;
        this.c21OrderEmailContentProvider = c21OrderEmailContentProvider;
    }

    @EventListener
    public void sendNotificationToHmctsAdmin(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = hmctsEmailContentProvider
            .buildHmctsSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());
        String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();

        emailNotificationService.sendNotification(HMCTS_COURT_SUBMISSION_TEMPLATE.getTemplateId(),
            email, parameters, reference);
    }

    @EventListener
    public void sendNotificationForC2Upload(final C2UploadedEvent caseEvent) {
        List<String> roles = userDetailsService.getUserDetails(caseEvent.getAuthorization()).getRoles();

        if (!roles.containsAll(UserRole.HMCTS_ADMIN.getRoles())) {
            CaseDetails caseDetailsFromEvent = caseEvent.getCallbackRequest().getCaseDetails();
            String localAuthorityCode = (String) caseDetailsFromEvent.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);

            Map<String, Object> parameters = c2UploadedEmailContentProvider.buildC2UploadNotification(
                caseDetailsFromEvent);
            String reference = Long.toString(caseDetailsFromEvent.getId());

            String email = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();
            emailNotificationService.sendNotification(C2_UPLOAD_NOTIFICATION_TEMPLATE.getTemplateId(),
                email, parameters, reference);
        }
    }

    @EventListener
    public void sendNotificationForC21Order(final C21OrderEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);

        Map<String, Object> parameters = c21OrderEmailContentProvider.buildC21OrderNotification(
            caseDetails, localAuthorityCode);
        String reference = Long.toString(caseDetails.getId());

        String localAuthorityEmail = hmctsCourtLookupConfiguration.getCourt(localAuthorityCode).getEmail();
        String cafcassEmail = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        emailNotificationService.sendNotification(C21_ORDER_NOTIFICATION_TEMPLATE.getTemplateId(),
            Arrays.asList(localAuthorityEmail, cafcassEmail), parameters, reference);
    }

    @EventListener
    public void sendNotificationToCafcass(SubmittedCaseEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        Map<String, Object> parameters = cafcassEmailContentProvider
            .buildCafcassSubmissionNotification(caseDetails, localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());
        String email = cafcassLookupConfiguration.getCafcass(localAuthorityCode).getEmail();

        emailNotificationService.sendNotification(CAFCASS_SUBMISSION_TEMPLATE.getTemplateId(),
            email, parameters, reference);
    }

    @EventListener
    public void sendNotificationToGatekeeper(NotifyGatekeeperEvent event) {
        CaseDetails caseDetails = event.getCallbackRequest().getCaseDetails();
        String localAuthorityCode = (String) caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        String email = (String) caseDetails.getData().get("gateKeeperEmail");
        Map<String, Object> parameters = gatekeeperEmailContentProvider.buildGatekeeperNotification(caseDetails,
            localAuthorityCode);
        String reference = String.valueOf(caseDetails.getId());

        emailNotificationService.sendNotification(GATEKEEPER_SUBMISSION_TEMPLATE.getTemplateId(),
            email, parameters, reference);
    }
}
