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
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageReplyEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.MessageJudgeService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions.REPLY;
import static uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData.transientFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/message-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeController extends CallbackController {
    private final MessageJudgeService messageJudgeService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(messageJudgeService.initialiseCaseFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        if (isReplyingToAMessage(caseData)) {
            caseDetailsMap.putAll(messageJudgeService.populateReplyMessageFields(caseData));
        } else {
            caseDetailsMap.putAll(messageJudgeService.populateNewMessageFields(caseData));
        }

        caseDetailsMap.put("nextHearingLabel", messageJudgeService.getFirstHearingLabel(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        List<Element<JudicialMessage>> updatedMessages;

        if (isReplyingToAMessage(caseData)) {
            updatedMessages = messageJudgeService.replyToJudicialMessage(caseData);
        } else {
            updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        }

        caseDetailsMap.put("judicialMessages", messageJudgeService.sortJudicialMessages(updatedMessages));

        removeTemporaryFields(caseDetailsMap, transientFields());

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Object judicialMessageDynamicList = caseData.getMessageJudgeEventData().getJudicialMessageDynamicList();
        JudicialMessage newJudicialMessage = caseData.getJudicialMessages().get(0).getValue();

        if (isNull(judicialMessageDynamicList)) {
            publishEvent(new NewJudicialMessageEvent(caseData, newJudicialMessage));
        } else {
            publishEvent(new NewJudicialMessageReplyEvent(caseData, newJudicialMessage));
        }
    }

    private boolean isReplyingToAMessage(CaseData caseData) {
        return REPLY.equals(caseData.getMessageJudgeEventData().getMessageJudgeOption());
    }
}
