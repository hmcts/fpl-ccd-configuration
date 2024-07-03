package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.JudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.ReplyToMessageJudgeService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData.transientFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@RestController
@RequestMapping("/callback/reply-message-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReplyToMessageJudgeController extends CallbackController {
    private final ReplyToMessageJudgeService replyToMessageJudgeService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(replyToMessageJudgeService.initialiseCaseFields(caseData));

        // We need to remove this field, as we might be closing the message, not replying
        caseDetailsMap.remove("latestRoleSent");

        return respond(caseDetailsMap);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(replyToMessageJudgeService.populateReplyMessageFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/validate/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateReplyMessage(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = replyToMessageJudgeService.validateJudgeReplyMessage(caseData);

        return respond(caseDetails, errors);
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(replyToMessageJudgeService.updateJudicialMessages(caseData));

        removeTemporaryFields(caseDetailsMap, transientFields());

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        if (!CollectionUtils.isEmpty(caseData.getClosedJudicialMessages())) {
            judicialMessages.addAll(caseData.getClosedJudicialMessages());
        }

        judicialMessages = replyToMessageJudgeService.sortJudicialMessages(judicialMessages);
        JudicialMessage judicialMessage = judicialMessages.get(0).getValue();

        if (OPEN.equals(judicialMessage.getStatus())) {
            publishEvent(new JudicialMessageReplyEvent(caseData, judicialMessage));
        }

    }
}
