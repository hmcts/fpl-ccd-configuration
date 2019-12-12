package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderErrorMessages.HEARING_NOT_COMPLETED;

@Service
public class CaseManagementOrderProgressionService {
    //TODO: better CCD ids for the below:
    // sharedDraftCMODocument -> sharedCaseManagementOrderDocument
    // caseManagementOrder -> draftCaseManagementOrder_LOCAL_AUTHORITY
    // cmoToAction -> draftCaseManagementOrder_JUDICIARY
    // requires changes in CCD definition. Decided not in scope of 24.

    private static final String SHARED_DRAFT_CMO_DOCUMENT_KEY = "sharedDraftCMODocument";
    private static final String LA_CMO_KEY = "caseManagementOrder";
    private static final String JUDGE_CMO_KEY = "cmoToAction";

    private final Time time;
    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;

    @Autowired
    public CaseManagementOrderProgressionService(Time time,
                                                 HearingBookingService hearingBookingService,
                                                 ObjectMapper mapper) {
        this.time = time;
        this.hearingBookingService = hearingBookingService;
        this.mapper = mapper;
    }

    public void handleCaseManagementOrderProgression(CaseDetails caseDetails, List<String> errors) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getCaseManagementOrder() != null) {
            progressDraftCaseManagementOrder(caseDetails, caseData.getCaseManagementOrder());
        } else {
            progressActionCaseManagementOrder(caseDetails, caseData, errors);
        }
    }

    private void progressDraftCaseManagementOrder(CaseDetails caseDetails, CaseManagementOrder order) {
        switch (order.getStatus()) {
            case SEND_TO_JUDGE:
                caseDetails.getData().put(JUDGE_CMO_KEY, order);
                caseDetails.getData().remove(LA_CMO_KEY);
                break;
            case PARTIES_REVIEW:
                caseDetails.getData().put(SHARED_DRAFT_CMO_DOCUMENT_KEY, order.getOrderDoc());
                break;
            case SELF_REVIEW:
                caseDetails.getData().remove(SHARED_DRAFT_CMO_DOCUMENT_KEY);
                break;
        }
    }

    private void progressActionCaseManagementOrder(CaseDetails caseDetails, CaseData caseData, List<String> errors) {
        switch (caseData.getCmoToAction().getAction().getType()) {
            case SEND_TO_ALL_PARTIES:
                LocalDateTime date = hearingBookingService
                    .getHearingBookingByUUID(caseData.getHearingDetails(), caseData.getCmoToAction().getId())
                    .getStartDate();

                if (date.isAfter(time.now())) {
                    List<Element<CaseManagementOrder>> orders = caseData.getServedCaseManagementOrders();
                    orders.add(0, Element.<CaseManagementOrder>builder()
                        .id(randomUUID())
                        .value(caseData.getCmoToAction())
                        .build());

                    caseDetails.getData().put("servedCaseManagementOrders", orders);
                    caseDetails.getData().remove(JUDGE_CMO_KEY);
                } else {
                    errors.add(HEARING_NOT_COMPLETED.getValue());
                }

                break;
            case JUDGE_REQUESTED_CHANGE:
                caseDetails.getData().put(LA_CMO_KEY, caseData.getCmoToAction());
                caseDetails.getData().remove(JUDGE_CMO_KEY);
                break;
            case SELF_REVIEW:
                break;
        }
    }
}
