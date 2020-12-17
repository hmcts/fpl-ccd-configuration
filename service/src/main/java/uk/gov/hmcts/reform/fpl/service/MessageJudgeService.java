package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MessageJudgeService {
    private final Time time;
    private final IdentityService identityService;
    private final ObjectMapper mapper;
    private final UserService userService;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasC2Documents(caseData)) {
            data.put("hasC2Applications", YES.getValue());
            data.put("c2DynamicList", caseData.buildC2DocumentDynamicList());
        }

        if (hasJudicialMessages(caseData)) {
            data.put("hasJudicialMessages", YES.getValue());
            data.put("judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());
        }

        return data;
    }

    public Map<String, Object> populateNewMessageFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasSelectedC2(caseData)) {
            UUID selectedC2Id = getDynamicListSelectedValue(
                caseData.getMessageJudgeEventData().getC2DynamicList(), mapper
            );

            C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);
            String documentFileNames = selectedC2DocumentBundle.getAllC2DocumentFileNames();

            data.put("relatedDocumentsLabel", documentFileNames);
            data.put("c2DynamicList", rebuildC2DynamicList(caseData, selectedC2Id));
        }

        return data;
    }

    public Map<String, Object> populateReplyMessageFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        UUID selectedJudicialMessageId = getDynamicListSelectedValue(
            caseData.getMessageJudgeEventData().getJudicialMessageDynamicList(), mapper);

        Optional<Element<JudicialMessage>> selectedJudicialMessageElement
            = findElement(selectedJudicialMessageId, caseData.getJudicialMessages());

        if (selectedJudicialMessageElement.isEmpty()) {
            throw new JudicialMessageNotFoundException(selectedJudicialMessageId);
        }

        JudicialMessage selectedJudicialMessage = selectedJudicialMessageElement.get().getValue();

        JudicialMessage judicialMessageReply = JudicialMessage.builder()
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .recipient(selectedJudicialMessage.getSender())
            .requestedBy(selectedJudicialMessage.getRequestedBy())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage("")
            .build();

        data.put("judicialMessageReply", judicialMessageReply);
        data.put("judicialMessageDynamicList", rebuildJudicialMessageDynamicList(caseData, selectedJudicialMessageId));

        return data;
    }

    public List<Element<JudicialMessage>> addNewJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessageMetaData judicialMessageMetaData = messageJudgeEventData.getJudicialMessageMetaData();
        String latestMessage = messageJudgeEventData.getJudicialMessageNote();

        String sender = userService.getUserEmail();

        JudicialMessage.JudicialMessageBuilder<?, ?> judicialMessageBuilder = JudicialMessage.builder()
            .sender(sender)
            .recipient(judicialMessageMetaData.getRecipient())
            .requestedBy(judicialMessageMetaData.getRequestedBy())
            .latestMessage(latestMessage)
            .messageHistory(String.format("%s - %s", sender, latestMessage))
            .updatedTime(time.now())
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .urgency(judicialMessageMetaData.getUrgency())
            .status(OPEN);

        if (hasSelectedC2(caseData)) {
            UUID selectedC2Id = getDynamicListSelectedValue(messageJudgeEventData.getC2DynamicList(), mapper);
            C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);

            judicialMessageBuilder.relatedDocuments(selectedC2DocumentBundle.getAllC2DocumentReferences());
            judicialMessageBuilder.relatedDocumentFileNames(selectedC2DocumentBundle.getAllC2DocumentFileNames());
            judicialMessageBuilder.isRelatedToC2(YES);
        }

        judicialMessages.add(element(identityService.generateId(), judicialMessageBuilder.build()));
        return judicialMessages;
    }

    public List<Element<JudicialMessage>> replyToJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessage judicialMessageReply = messageJudgeEventData.getJudicialMessageReply();

        UUID selectedJudicialMessageId = getDynamicListSelectedValue(
            caseData.getMessageJudgeEventData().getJudicialMessageDynamicList(), mapper
        );

        return judicialMessages.stream()
            .map(judicialMessageElement -> {
                if (selectedJudicialMessageId.equals(judicialMessageElement.getId())) {

                    JudicialMessage judicialMessage = judicialMessageElement.getValue();

                    String sender = userService.getUserEmail();

                    JudicialMessage updatedMessage = judicialMessage.toBuilder()
                        .updatedTime(time.now())
                        .sender(sender) // Get the email of the current user
                        .recipient(judicialMessage.getSender()) // Get the sender of the previous message
                        .messageHistory(String.join("\n \n", List.of(
                            judicialMessage.getMessageHistory(),
                            String.format("%s - %s", sender, judicialMessageReply.getLatestMessage()))))
                        .latestMessage(judicialMessageReply.getLatestMessage())
                        .build();

                    return element(judicialMessageElement.getId(), updatedMessage);
                }

                return judicialMessageElement;
            }).collect(Collectors.toList());
    }

    public List<Element<JudicialMessage>> sortJudicialMessages(List<Element<JudicialMessage>> judicialMessages) {
        judicialMessages.sort(Comparator.comparing(judicialMessageElement
            -> judicialMessageElement.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        return judicialMessages;
    }

    public String getFirstHearingLabel(CaseData caseData) {
        return caseData.getFirstHearing()
            .map(hearing -> String.format("Next hearing in the case: %s", hearing.toLabel()))
            .orElse("");
    }

    private DynamicList rebuildC2DynamicList(CaseData caseData, UUID selectedC2Id) {
        return caseData.buildC2DocumentDynamicList(selectedC2Id);
    }

    private DynamicList rebuildJudicialMessageDynamicList(CaseData caseData, UUID selectedC2Id) {
        return caseData.buildJudicialMessageDynamicList(selectedC2Id);
    }

    private boolean hasC2Documents(CaseData caseData) {
        return caseData.getC2DocumentBundle() != null;
    }

    private boolean hasSelectedC2(CaseData caseData) {
        return hasC2Documents(caseData)
            && caseData.getMessageJudgeEventData().getC2DynamicList() != null;
    }

    private boolean hasJudicialMessages(CaseData caseData) {
        return caseData.getJudicialMessages() != null && !caseData.getJudicialMessages().isEmpty();
    }
}
