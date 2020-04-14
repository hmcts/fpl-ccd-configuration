package uk.gov.hmcts.reform.fpl.model.event;

import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.CallbackEvent;

@Getter
public class EventData {
    private static final String CASE_LOCAL_AUTHORITY_PROPERTY_NAME = "caseLocalAuthority";

    private CaseDetails caseDetails;
    private String localAuthorityCode;
    private String reference;

    public EventData(CallbackEvent event) {
        this.caseDetails = event.getCallbackRequest().getCaseDetails();
        this.localAuthorityCode = (String) this.caseDetails.getData().get(CASE_LOCAL_AUTHORITY_PROPERTY_NAME);
        this.reference = Long.toString(this.caseDetails.getId());
    }
}
