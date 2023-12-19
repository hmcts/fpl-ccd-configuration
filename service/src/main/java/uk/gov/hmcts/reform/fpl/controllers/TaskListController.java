package uk.gov.hmcts.reform.fpl.controllers;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.CaseDataChanged;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListController extends CallbackController {

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackrequest) {
        final CaseData caseData = getCaseData(callbackrequest);
        final CaseDetailsMap caseDetails = caseDetailsMap(callbackrequest.getCaseDetails());

        caseDetails.putIfNotEmpty("caseNameHmctsInternal", caseData.getCaseName());

        return respond(caseDetails);
    }

    @PostMapping("/enter-c1-with-supplement/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleEnterC1WithSupplementAboutToStart(
        @RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (caseData.getSubmittedC1WithSupplement() != null) {
            caseDetails.getData().put("submittedC1WithSupplement", caseData.getSubmittedC1WithSupplement());
        }

        return respond(caseDetails);
    }

    @PostMapping("/enter-c1-with-supplement/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleEnterC1WithSupplementAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest);
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        if (YesNo.YES.getValue().equalsIgnoreCase(Optional.ofNullable(caseData.getSubmittedC1WithSupplement())
            .orElse(SubmittedC1WithSupplementBundle.builder().clearSubmittedC1WithSupplement(YesNo.NO.getValue())
                .build())
            .getClearSubmittedC1WithSupplement())) {
            caseDetails.getData().remove("submittedC1WithSupplement");
        } else if (caseData.getSubmittedC1WithSupplement() != null) {
            caseDetails.getData().put("submittedC1WithSupplement", caseData.getSubmittedC1WithSupplement());
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        publishEvent(new CaseDataChanged(getCaseData(callbackRequest)));
    }
}
