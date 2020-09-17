package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.FINAL_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/change-state")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ChangeStateController extends CallbackController {

    private static final String LABEL_CONTENT = "Do you want to change the case state to %s?";

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put(
            "nextStateLabelContent",
            String.format(LABEL_CONTENT, nextState(caseData).getValue().toLowerCase())
        );

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (YesNo.fromString(caseData.getChangeState()) == YesNo.YES) {
            caseDetails.getData().put("state", nextState(caseData));
        }

        removeTemporaryFields(caseDetails, "changeState", "nextStateLabelContent");

        return respond(caseDetails);
    }

    private State nextState(CaseData caseData) {
        switch (caseData.getState()) {
            case CASE_MANAGEMENT:
                return FINAL_HEARING;
            case FINAL_HEARING:
                return CASE_MANAGEMENT;
            default:
                throw new IllegalStateException("Should not be able to change from: " + caseData.getState());
        }
    }
}
