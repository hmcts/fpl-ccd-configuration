package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.fpl.NotifyTemplates;

public enum NotificationHandlerType {
    HMCTS_ADMIN_SUBMISSION_NOTIFICATION(NotifyTemplates.HMCTS_COURT_SUBMISSION_TEMPLATE),
    CAFCASS_SUBMISSION_NOTIFICATION(NotifyTemplates.CAFCASS_SUBMISSION_TEMPLATE),
    GATEKEEPER_SUBMISSION_NOTIFICATION(NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE),
    C2_UPLOAD_NOTIFICATION(NotifyTemplates.C2_UPLOAD_SUBMISSION_TEMPLATE),
    C21_ORDER_NOTIFICATION(NotifyTemplates.C21_ORDER_SUBMISSION_TEMPLATE);

    private final String emailTemplateId;

    NotificationHandlerType(String emailTemplateId) {
        this.emailTemplateId = emailTemplateId;
    }
}
