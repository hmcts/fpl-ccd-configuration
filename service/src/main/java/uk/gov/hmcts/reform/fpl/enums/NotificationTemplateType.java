package uk.gov.hmcts.reform.fpl.enums;

public enum NotificationTemplateType {

    HMCTS_COURT_SUBMISSION_TEMPLATE("9b8142bf-badd-4686-8d7d-3035fa01548e"),
    CAFCASS_SUBMISSION_TEMPLATE("1c8e0794-197d-4e32-94c4-60111216dc89"),
    GATEKEEPER_SUBMISSION_TEMPLATE("c1d5d634-f654-4afc-9e35-3e9e96f7a07c"),
    C2_UPLOAD_NOTIFICATION_TEMPLATE("6b961e81-c5ff-4f6f-8c56-f90d932a2f9b"),
    C21_ORDER_NOTIFICATION_TEMPLATE("1f7c134e-f9c0-44ba-aa50-fce53eb208f7");

    private final String templateId;

    NotificationTemplateType(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }
}
