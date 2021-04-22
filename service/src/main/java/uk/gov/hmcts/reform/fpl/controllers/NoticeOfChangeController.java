package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;

import java.util.List;

@Api
@RestController
@RequestMapping("/callback/check-noc-approval")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeController extends CallbackController {
    private final NoticeOfChangeService noticeOfChangeService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        List<Element<Respondent>> updatedRespondents =
            noticeOfChangeService.updateRespondentsOnNoc(caseDetails, caseDetailsBefore);

        caseDetails.getData().put("respondents1", updatedRespondents);

        return respond(caseDetails);
    }
}
