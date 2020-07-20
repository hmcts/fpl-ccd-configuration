package uk.gov.hmcts.reform.fpl.enums;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

/**
 * Status of CMO that judge can select, decides the flow of the CMO at the end of
 * {@link uk.gov.hmcts.reform.fpl.controllers.ActionCaseManagementOrderController#handleSubmitted(CallbackRequest)
 * Action CMO flow}.
 *
 * @deprecated to be removed, will be using {@link CMOStatus} hopefully in rework
 */
@Deprecated(since = "FPLA-1915")
public enum ActionType {
    SEND_TO_ALL_PARTIES,
    JUDGE_REQUESTED_CHANGE,
    SELF_REVIEW
}
