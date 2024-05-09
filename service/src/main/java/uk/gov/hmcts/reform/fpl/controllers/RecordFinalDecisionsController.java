package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.RecordFinalDecisionsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@RestController
@RequestMapping("/callback/record-final-decisions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecordFinalDecisionsController extends CallbackController {


    public static final String CLOSE_CASE_TAB_FIELD = "closeCaseTabField";
    private final ChildrenService childrenService;
    private final RecordFinalDecisionsService recordFinalDecisionService;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        data.putAll(recordFinalDecisionService.prePopulateFields(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/pre-populate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventPrePopulation(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        data.putAll(recordFinalDecisionService.populateFields(caseData));

        List<String> errors = recordFinalDecisionService.validateChildSelector(caseData);

        return respond(caseDetails, errors);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventValidation(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = recordFinalDecisionService.validateFinalDecisionDate(caseData);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = getCaseData(caseDetails);
        RecordChildrenFinalDecisionsEventData eventData = caseData.getRecordChildrenFinalDecisionsEventData();

        List<Element<Child>> updatedChildren = recordFinalDecisionService.updateChildren(caseData);

        data.put("children1", updatedChildren);

        if (childrenService.allChildrenHaveFinalOrderOrDecision(updatedChildren)) {
            LocalDate closeCaseDate = eventData.getFinalDecisionDate();
            data.put("state", CLOSED);
            data.put(CLOSE_CASE_TAB_FIELD, CloseCase.builder().date(closeCaseDate).build());
        }

        removeTemporaryFields(caseDetails, eventData.getTransientFields());

        return respond(caseDetails);
    }
}
