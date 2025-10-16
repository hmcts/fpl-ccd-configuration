package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
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

    @Autowired
    private FeatureToggleService featureToggleService;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        data.put("hasJudicialMessages", (caseData.getJudicialMessages().isEmpty()) ? NO : YES);
        data.put("judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());

        data.put("isSendingEmailsInCourt",
            YesNo.from(featureToggleService.isCourtNotificationEnabledForWa(caseData.getCourt())));

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

        JudicialMessageRoleType senderRole = getSenderRole(caseData);

        JudicialMessage judicialMessageReply = JudicialMessage.builder()
            .relatedDocumentFileNames(selectedJudicialMessage.getRelatedDocumentFileNames())
            .recipientLabel(formatLabel(selectedJudicialMessage.getSenderType(), selectedJudicialMessage.getSender()))
            .senderType(senderRole)
            .relatedDocuments(selectedJudicialMessage.getRelatedDocuments())
            .subject(selectedJudicialMessage.getSubject())
            .recipientDynamicList(buildRecipientDynamicList(
                caseData, senderRole, Optional.of(selectedJudicialMessage.getSenderType().toString())))
            .urgency(selectedJudicialMessage.getUrgency())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage(EMPTY)
            .replyFrom(getSenderEmailAddressByRoleType(senderRole))
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
                selectedJudicialMessageId, caseData.getJudicialMessages(), caseData.getClosedJudicialMessages(),
                judicialMessageReply.getClosureNote());
        } else {
            List<Element<JudicialMessage>> updatedMessages = replyToJudicialMessage(
                selectedJudicialMessageId, judicialMessageReply, caseData.getJudicialMessages(), caseData);
            return Map.of("judicialMessages", sortJudicialMessages(updatedMessages),
                "latestRoleSent", JudicialMessageRoleType.valueOf(
                    judicialMessageReply.getRecipientDynamicList().getValueCode()));
        }
    }

    private Map<String, Object> closeJudicialMessage(UUID selectedJudicialMessageId,
                                                     List<Element<JudicialMessage>> openJudicialMessages,
                                                     List<Element<JudicialMessage>> closedJudicialMessages,
                                                     String closureNote) {

        Element<JudicialMessage> judicialMessageElement = openJudicialMessages.stream()
            .filter(message -> selectedJudicialMessageId.equals(message.getId()))
            .findFirst()
            .orElseThrow(() -> new JudicialMessageNotFoundException(selectedJudicialMessageId));

        List<Element<JudicialMessage>> updatedJudicialMessages = new ArrayList<>(openJudicialMessages);
        updatedJudicialMessages.remove(judicialMessageElement); // remove from open judicial messages

        List<Element<JudicialMessage>> updatedClosedJudicialMessages = new ArrayList<>(
            Optional.ofNullable(closedJudicialMessages).orElse(newArrayList()));

        updatedClosedJudicialMessages.add(element(judicialMessageElement.getId(),
            judicialMessageElement.getValue().toBuilder()
                .status(CLOSED)
                .closureNote(closureNote)
                .updatedTime(time.now())
                .build()));

        return Map.of("judicialMessages", updatedJudicialMessages,
            "closedJudicialMessages", sortJudicialMessages(updatedClosedJudicialMessages));
    }

    private List<Element<JudicialMessage>> replyToJudicialMessage(UUID selectedJudicialMessageId,
                                                                  JudicialMessage judicialMessageReply,
                                                                  List<Element<JudicialMessage>> judicialMessages,
                                                                  CaseData caseData) {
        return judicialMessages.stream()
            .map(judicialMessageElement -> {
                if (selectedJudicialMessageId.equals(judicialMessageElement.getId())) {

                    JudicialMessage judicialMessage = judicialMessageElement.getValue();

                    String sender = getSenderEmailAddressByRoleType(judicialMessageReply.getSenderType());

                    JudicialMessageRoleType recipientType = JudicialMessageRoleType.valueOf(
                        judicialMessageReply.getRecipientDynamicList().getValueCode());

                    String recipientEmail = resolveRecipientEmailAddress(
                        recipientType, judicialMessageReply.getReplyTo(), caseData);

                    JudicialMessage updatedMessage = judicialMessage.toBuilder()
                        .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
                        .updatedTime(time.now())
                        .senderType(judicialMessageReply.getSenderType())
                        .sender(sender)
                        .recipientType(recipientType)
                        .recipient(recipientEmail)
                        .fromLabel(formatLabel(judicialMessageReply.getSenderType(), sender))
                        .toLabel(formatLabel(recipientType, recipientEmail))
                        .recipientDynamicList(null)
                        .messageHistory(buildMessageHistory(judicialMessageReply, judicialMessage,
                            formatLabel(judicialMessageReply.getSenderType(), sender)))
                        .closureNote(judicialMessageReply.getClosureNote())
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
}
