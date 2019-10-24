package uk.gov.hmcts.reform.fpl.factory;

import uk.gov.hmcts.reform.fpl.enums.NotificationHandlerType;
import uk.gov.hmcts.reform.fpl.interfaces.INotifier;

public class EmailNotifier implements INotifier {

    public void generateNotification(NotificationHandlerType notificationHandlerType){

        switch (notificationHandlerType) {
            case HMCTS_ADMIN_SUBMISSION_NOTIFICATION:
                break;
            case GATEKEEPER_SUBMISSION_NOTIFICATION:
                break;
            case CAFCASS_SUBMISSION_NOTIFICATION:
                break;
            case C2_UPLOAD_NOTIFICATION:
                break;
            case C21_ORDER_NOTIFICATION:
                break;
        }
    }
}
