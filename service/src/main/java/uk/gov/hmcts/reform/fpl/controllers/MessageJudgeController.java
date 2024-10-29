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
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.events.NewJudicialMessageEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.SendNewMessageJudgeService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData.transientFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@RestController
@RequestMapping("/callback/message-judge")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeController extends CallbackController {
    private final SendNewMessageJudgeService messageJudgeService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(messageJudgeService.initialiseCaseFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/populate-lists/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleDocumentListEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(messageJudgeService.populateDynamicLists(caseData));
        List<String> errors = messageJudgeService.validateDynamicLists(caseData);

        return respond(caseDetailsMap, errors);
    }

    @PostMapping("/populate-document-labels/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        caseDetailsMap.putAll(messageJudgeService.populateNewMessageFields(caseData));

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        List<Element<JudicialMessage>> updatedMessages;

        Optional<String> emailError = messageJudgeService.validateRecipientEmail(caseData);
        if (!emailError.isEmpty()) {
            removeTemporaryFields(caseDetailsMap, transientFields());
            return respond(caseDetailsMap, List.of(emailError.get()));
        }

        updatedMessages = messageJudgeService.addNewJudicialMessage(caseData);
        caseDetailsMap.put("judicialMessages", messageJudgeService.sortJudicialMessages(updatedMessages));
        caseDetailsMap.put("latestRoleSent", caseData.getMessageJudgeEventData().getJudicialMessageMetaData()
            .getRecipientType());

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

        judicialMessages = messageJudgeService.sortJudicialMessages(judicialMessages);
        JudicialMessage judicialMessage = judicialMessages.get(0).getValue();

        if (OPEN.equals(judicialMessage.getStatus())) {
            publishEvent(new NewJudicialMessageEvent(caseData, judicialMessage));
        }

        publishEvent(new AfterSubmissionCaseDataUpdated(getCaseData(callbackRequest),
            getCaseDataBefore(callbackRequest)));

    }
}
