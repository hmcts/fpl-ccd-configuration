package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;

@Service
public class ReplyToMessageJudgeService extends MessageJudgeService {
    @Autowired
    private ObjectMapper mapper;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        data.put("hasJudicialMessages", (caseData.getJudicialMessages().isEmpty()) ? NO : YES);
        data.put("judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());

        return data;
    }

    public List<String> validateJudgeReplyMessage(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessage judicialMessageReply = messageJudgeEventData.getJudicialMessageReply();

        if (isReplyingToMessage(judicialMessageReply) && hasMatchingReplyEmailAddress(judicialMessageReply)) {
            errors.add("The sender's and recipient's email address cannot be the same");
        }

        return errors;
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
            .recipientType(selectedJudicialMessage.getSenderType())
            .recipient(selectedJudicialMessage.getSender())
            .subject(selectedJudicialMessage.getSubject())
            .urgency(selectedJudicialMessage.getUrgency())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage(EMPTY)
            .replyFrom(isJudiciary() ? getEmailAddressByRoleType(JudicialMessageRoleType.ALLOCATED_JUDGE) : EMPTY)
            .replyTo(selectedJudicialMessage.getSender())
            .build();

        data.put("judicialMessageReply", judicialMessageReply);
        data.put("judicialMessageDynamicList", rebuildJudicialMessageDynamicList(caseData, selectedJudicialMessageId));
        data.put("replyToMessageJudgeNextHearingLabel", getNextHearingLabel(caseData));

        if (isJudiciary()) {
            data.put("isJudiciary", YES);
        } else {
            data.put("isJudiciary", NO);
        }

        return data;
    }

    public Map<String, Object> updateJudicialMessages(CaseData caseData) {
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessage judicialMessageReply = messageJudgeEventData.getJudicialMessageReply();

        UUID selectedJudicialMessageId = getDynamicListSelectedValue(
            caseData.getMessageJudgeEventData().getJudicialMessageDynamicList(), mapper
        );

        if (NO.getValue().equals(judicialMessageReply.getIsReplying())) {
            return closeJudicialMessage(
                selectedJudicialMessageId, caseData.getJudicialMessages(), caseData.getClosedJudicialMessages());
        } else {
            List<Element<JudicialMessage>> updatedMessages = replyToJudicialMessage(
                selectedJudicialMessageId, judicialMessageReply, caseData.getJudicialMessages());
            return Map.of("judicialMessages", sortJudicialMessages(updatedMessages),
                "latestRoleSent", judicialMessageReply.getRecipientType());
        }
    }

    private Map<String, Object> closeJudicialMessage(UUID selectedJudicialMessageId,
                                                     List<Element<JudicialMessage>> openJudicialMessages,
                                                     List<Element<JudicialMessage>> closedJudicialMessages) {

        Element<JudicialMessage> judicialMessageElement = openJudicialMessages.stream()
            .filter(message -> selectedJudicialMessageId.equals(message.getId()))
            .findFirst()
            .orElseThrow(() -> new JudicialMessageNotFoundException(selectedJudicialMessageId));

        List<Element<JudicialMessage>> updatedJudicialMessages = new ArrayList<>(openJudicialMessages);
        updatedJudicialMessages.remove(judicialMessageElement); // remove from open judicial messages

        List<Element<JudicialMessage>> updatedClosedJudicialMessages = new ArrayList<>(
            Optional.ofNullable(closedJudicialMessages).orElse(newArrayList()));

        updatedClosedJudicialMessages.add(element(judicialMessageElement.getId(),
            judicialMessageElement.getValue().toBuilder().status(CLOSED).updatedTime(time.now()).build()));

        return Map.of("judicialMessages", updatedJudicialMessages,
            "closedJudicialMessages", sortJudicialMessages(updatedClosedJudicialMessages));
    }

    private List<Element<JudicialMessage>> replyToJudicialMessage(UUID selectedJudicialMessageId,
                                                                  JudicialMessage judicialMessageReply,
                                                                  List<Element<JudicialMessage>> judicialMessages) {
        return judicialMessages.stream()
            .map(judicialMessageElement -> {
                if (selectedJudicialMessageId.equals(judicialMessageElement.getId())) {

                    JudicialMessage judicialMessage = judicialMessageElement.getValue();

                    String sender = resolveSenderEmailAddress(judicialMessageReply.getSenderType(),
                        judicialMessageReply.getReplyFrom());

                    JudicialMessage updatedMessage = judicialMessage.toBuilder()
                        .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
                        .updatedTime(time.now())
                        .senderType(resolveSenderRoleType(judicialMessageReply.getSenderType()))
                        .sender(sender)
                        .recipientType(judicialMessageReply.getRecipientType())
                        .recipient(resolveRecipientEmailAddress(judicialMessageReply.getRecipientType(),
                            judicialMessageReply.getReplyTo()))
                        .messageHistory(buildMessageHistory(judicialMessageReply, judicialMessage, sender))
                        .latestMessage(judicialMessageReply.getLatestMessage())
                        .build();

                    return element(judicialMessageElement.getId(), updatedMessage);
                }

                return judicialMessageElement;
            }).collect(toList());
    }

    private String buildMessageHistory(JudicialMessage reply, JudicialMessage previousMessage, String sender) {
        return buildMessageHistory(reply.getLatestMessage(), previousMessage.getMessageHistory(), sender);
    }

    private DynamicList rebuildJudicialMessageDynamicList(CaseData caseData, UUID selectedC2Id) {
        return caseData.buildJudicialMessageDynamicList(selectedC2Id);
    }

    private boolean hasMatchingReplyEmailAddress(JudicialMessage judicialMessageReply) {
        String sender = resolveSenderEmailAddress(judicialMessageReply.getSenderType(),
            judicialMessageReply.getReplyFrom());
        String recipient = resolveRecipientEmailAddress(judicialMessageReply.getRecipientType(),
            judicialMessageReply.getReplyTo());

        return isNotEmpty(sender) && sender.equals(recipient);
    }

    private boolean isReplyingToMessage(JudicialMessage judicialMessageReply) {
        return judicialMessageReply != null && YES.getValue().equals(judicialMessageReply.getIsReplying());
    }
}
