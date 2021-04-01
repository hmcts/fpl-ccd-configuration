package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.JudicialMessageNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
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
    private final CtscEmailLookupConfiguration ctscEmailLookupConfiguration;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasAdditionalApplicationDocuments(caseData) || hasC2Documents(caseData)) {
            data.put("hasAdditionalApplications", YES.getValue());
            data.put("additionalApplicationsDynamicList", caseData.buildApplicationBundlesDynamicList());
        }
        if (hasC2Documents(caseData)) {
            data.put("hasC2Applications", YES.getValue());
            data.put("c2DynamicList", caseData.buildC2DocumentDynamicList());
        }

        if (hasJudicialMessages(caseData)) {
            data.put("hasJudicialMessages", YES.getValue());
            data.put("judicialMessageDynamicList", caseData.buildJudicialMessageDynamicList());
        }

        data.putAll(prePopulateSenderAndRecipient());

        return data;
    }

    public Map<String, Object> populateNewMessageFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        String documentFileNames;

        if (hasSelectedAdditionalApplication(caseData)) {
            UUID selectedApplicationId = getDynamicListSelectedValue(
                caseData.getMessageJudgeEventData().getAdditionalApplicationsDynamicList(), mapper
            );

            ApplicationsBundle selectedApplicationBundle = caseData.getApplicationBundleByUUID(selectedApplicationId);

            if (selectedApplicationBundle instanceof C2DocumentBundle) {
                C2DocumentBundle c2DocumentBundle = (C2DocumentBundle) selectedApplicationBundle;
                documentFileNames = c2DocumentBundle.getAllC2DocumentFileNames();
            } else {
                OtherApplicationsBundle otherApplicationsBundle = (OtherApplicationsBundle) selectedApplicationBundle;
                documentFileNames = otherApplicationsBundle.getAllDocumentFileNames();
            }

            data.put("relatedDocumentsLabel", documentFileNames);
            data.put("additionalApplicationsDynamicList", rebuildAdditionalApplicationsDynamicList(
                caseData, selectedApplicationId)
            );

        } else if (hasSelectedC2(caseData)) {
            UUID selectedC2Id = getDynamicListSelectedValue(
                caseData.getMessageJudgeEventData().getC2DynamicList(), mapper
            );

            C2DocumentBundle selectedC2DocumentBundle = caseData.getC2DocumentBundleByUUID(selectedC2Id);
            documentFileNames = selectedC2DocumentBundle.getAllC2DocumentFileNames();

            data.put("relatedDocumentsLabel", documentFileNames);
            data.put("c2DynamicList", rebuildC2DynamicList(caseData, selectedC2Id));
        }

        return data;
    }

    public List<String> validateJudgeReplyMessage(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessage judicialMessageReply = messageJudgeEventData.getJudicialMessageReply();

        if (isReplyingToMessage(judicialMessageReply) && hasMatchingReplyEmaiAddress(judicialMessageReply)) {
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
            .recipient(selectedJudicialMessage.getSender())
            .subject(selectedJudicialMessage.getSubject())
            .urgency(selectedJudicialMessage.getUrgency())
            .messageHistory(selectedJudicialMessage.getMessageHistory())
            .latestMessage(EMPTY)
            .replyFrom(getReplyFrom())
            .replyTo(selectedJudicialMessage.getSender())
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

        String sender = judicialMessageMetaData.getSender();

        JudicialMessage.JudicialMessageBuilder<?, ?> judicialMessageBuilder = JudicialMessage.builder()
            .sender(sender)
            .recipient(judicialMessageMetaData.getRecipient())
            .subject(judicialMessageMetaData.getSubject())
            .latestMessage(latestMessage)
            .messageHistory(buildMessageHistory(latestMessage, sender))
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
        if (hasSelectedAdditionalApplication(caseData)) {
            UUID selectedApplicationId =
                getDynamicListSelectedValue(messageJudgeEventData.getAdditionalApplicationsDynamicList(),
                    mapper);
            ApplicationsBundle selectedApplicationBundle = caseData.getApplicationBundleByUUID(selectedApplicationId);

            if (selectedApplicationBundle instanceof C2DocumentBundle) {
                C2DocumentBundle selectedC2Bundle = (C2DocumentBundle) selectedApplicationBundle;
                judicialMessageBuilder.relatedDocuments(selectedC2Bundle.getAllC2DocumentReferences());
                judicialMessageBuilder.relatedDocumentFileNames(selectedC2Bundle.getAllC2DocumentFileNames());
            } else {
                OtherApplicationsBundle selectedOtherApplicationsBundle =
                    (OtherApplicationsBundle) selectedApplicationBundle;
                judicialMessageBuilder.relatedDocuments(selectedOtherApplicationsBundle.getAllDocumentReferences());
                judicialMessageBuilder.relatedDocumentFileNames(
                    selectedOtherApplicationsBundle.getAllDocumentFileNames()
                );
            }

            judicialMessageBuilder.applicationType(selectedApplicationBundle.toLabel());
        }

        judicialMessages.add(element(identityService.generateId(), judicialMessageBuilder.build()));
        return judicialMessages;
    }

    public List<Element<JudicialMessage>> sortJudicialMessages(List<Element<JudicialMessage>> judicialMessages) {
        judicialMessages.sort(Comparator.comparing(judicialMessageElement
            -> judicialMessageElement.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        return judicialMessages;
    }

    public String getNextHearingLabel(CaseData caseData) {
        return caseData.getNextHearingAfter(time.now())
            .map(hearing -> String.format("Next hearing in the case: %s", hearing.toLabel()))
            .orElse("");
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
            return Map.of("judicialMessages", sortJudicialMessages(updatedMessages));
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

                    String sender = judicialMessageReply.getReplyFrom();

                    JudicialMessage updatedMessage = judicialMessage.toBuilder()
                        .updatedTime(time.now())
                        .sender(sender)
                        .recipient(judicialMessageReply.getReplyTo())
                        .messageHistory(buildMessageHistory(judicialMessageReply, judicialMessage, sender))
                        .latestMessage(judicialMessageReply.getLatestMessage())
                        .build();

                    return element(judicialMessageElement.getId(), updatedMessage);
                }

                return judicialMessageElement;
            }).collect(Collectors.toList());
    }

    private String buildMessageHistory(String message, String sender) {
        return buildMessageHistory(message, "", sender);
    }

    private String buildMessageHistory(JudicialMessage reply, JudicialMessage previousMessage, String sender) {
        return buildMessageHistory(reply.getLatestMessage(), previousMessage.getMessageHistory(), sender);
    }

    private String buildMessageHistory(String message, String history, String sender) {
        String formattedLatestMessage = String.format("%s - %s", sender, message);

        if (history.isBlank()) {
            return formattedLatestMessage;
        }

        return String.join("\n \n", history, formattedLatestMessage);
    }

    private DynamicList rebuildC2DynamicList(CaseData caseData, UUID selectedC2Id) {
        return caseData.buildC2DocumentDynamicList(selectedC2Id);
    }

    private DynamicList rebuildAdditionalApplicationsDynamicList(CaseData caseData, UUID selectedApplicationId) {
        return caseData.buildApplicationBundlesDynamicList(selectedApplicationId);
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

    private boolean hasAdditionalApplicationDocuments(CaseData caseData) {
        return caseData.getAdditionalApplicationsBundle() != null;
    }

    private boolean hasSelectedAdditionalApplication(CaseData caseData) {
        return (hasAdditionalApplicationDocuments(caseData) || hasC2Documents(caseData))
            && caseData.getMessageJudgeEventData().getAdditionalApplicationsDynamicList() != null;
    }

    private boolean hasJudicialMessages(CaseData caseData) {
        return !caseData.getJudicialMessages().isEmpty();
    }

    private boolean hasMatchingReplyEmaiAddress(JudicialMessage judicialMessageReply) {
        return isNotEmpty(judicialMessageReply.getReplyFrom())
            && judicialMessageReply.getReplyFrom().equals(judicialMessageReply.getReplyTo());
    }

    private boolean isReplyingToMessage(JudicialMessage judicialMessageReply) {
        return judicialMessageReply != null && YES.getValue().equals(judicialMessageReply.getIsReplying());
    }

    private Map<String, Object> prePopulateSenderAndRecipient() {
        Map<String, Object> data = new HashMap<>();

        if (userService.hasUserRole(JUDICIARY)) {
            data.put("judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(userService.getUserEmail())
                .recipient(ctscEmailLookupConfiguration.getEmail()).build());
        } else {
            data.put("judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(ctscEmailLookupConfiguration.getEmail())
                .recipient(EMPTY).build());
        }

        return data;
    }

    private String getReplyFrom() {
        if (userService.hasUserRole(JUDICIARY)) {
            return userService.getUserEmail();
        } else {
            return ctscEmailLookupConfiguration.getEmail();
        }
    }

}
