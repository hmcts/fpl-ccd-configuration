package uk.gov.hmcts.reform.fpl.events;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public class CaseManagementOrderReadyForJudgeReviewEvent extends CallbackEvent {
    public CaseManagementOrderReadyForJudgeReviewEvent(CallbackRequest callbackRequest,
                                                       String authorization,
                                                       String userId) {
        super(callbackRequest, authorization, userId);
    }
}
