package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.SelectableItem;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateTimeFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Service
public class SendNewMessageJudgeService extends MessageJudgeService {
    @Autowired
    private ValidateEmailService validateEmailService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ObjectMapper mapper;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasAdditionalApplications(caseData) || hasC2s(caseData)) {
            data.put("hasAdditionalApplications", YES.getValue());
            data.put("additionalApplicationsDynamicList", getApplicationsLists(caseData, null));
        }
        data.putAll(prePopulateSenderAndRecipient());

        return data;
    }

    public Map<String, Object> populateNewMessageFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasSelectedAdditionalApplication(caseData)) {
            final UUID selectedApplicationId = getDynamicListSelectedValue(
                caseData.getMessageJudgeEventData().getAdditionalApplicationsDynamicList(), mapper);

            final List<Element<SelectableItem>> applications = getApplications(caseData);

            final SelectableItem selectedApplication = getElement(selectedApplicationId, applications).getValue();

            data.put("relatedDocumentsLabel", getRelatedDocumentNames(selectedApplication));
            data.put("additionalApplicationsDynamicList", getApplicationsLists(caseData, selectedApplicationId));
        }
        data.put("nextHearingLabel", getNextHearingLabel(caseData));

        return data;
    }

    public Optional<String> validateRecipientEmail(CaseData caseData) {
        JudicialMessageMetaData judgeMetaData = caseData.getMessageJudgeEventData().getJudicialMessageMetaData();
        if (nonNull(judgeMetaData)) {
            String email = resolveRecipientEmailAddress(judgeMetaData.getRecipientType(), judgeMetaData.getRecipient());
            return validateEmailService.validate(email);
        }
        return Optional.empty();
    }

    public List<Element<JudicialMessage>> addNewJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessageMetaData judicialMessageMetaData = messageJudgeEventData.getJudicialMessageMetaData();
        String latestMessage = messageJudgeEventData.getJudicialMessageNote();

        String sender = resolveSenderEmailAddress(judicialMessageMetaData.getSenderType(),
            judicialMessageMetaData.getSender());

        JudicialMessage.JudicialMessageBuilder<?, ?> judicialMessageBuilder = JudicialMessage.builder()
            .sender(sender)
            .senderType(resolveSenderRoleType(judicialMessageMetaData.getSenderType()))
            .recipient(resolveRecipientEmailAddress(judicialMessageMetaData.getRecipientType(),
                judicialMessageMetaData.getRecipient()))
            .recipientType(judicialMessageMetaData.getRecipientType())
            .subject(judicialMessageMetaData.getSubject())
            .latestMessage(latestMessage)
            .messageHistory(buildMessageHistory(latestMessage, sender))
            .updatedTime(time.now())
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .urgency(judicialMessageMetaData.getUrgency())
            .status(OPEN);

        if (hasSelectedAdditionalApplication(caseData)) {
            UUID selectedApplicationId =
                getDynamicListSelectedValue(messageJudgeEventData.getAdditionalApplicationsDynamicList(),
                    mapper);
            SelectableItem selectedApplicationBundle = getApplication(caseData, selectedApplicationId);

            if (selectedApplicationBundle instanceof C2DocumentBundle) {
                C2DocumentBundle selectedC2Bundle = (C2DocumentBundle) selectedApplicationBundle;
                judicialMessageBuilder.relatedDocuments(selectedC2Bundle.getAllC2DocumentReferences());
                judicialMessageBuilder.relatedDocumentFileNames(selectedC2Bundle.getAllC2DocumentFileNames());
            } else if (selectedApplicationBundle instanceof Placement) {
                Placement placement = (Placement) selectedApplicationBundle;
                judicialMessageBuilder.relatedDocuments(wrapElements(getRelatedDocuments(placement)));
                judicialMessageBuilder.relatedDocumentFileNames(getRelatedDocumentsNames(placement));
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

    private List<Element<SelectableItem>> getApplications(CaseData caseData) {

        final List<Element<SelectableItem>> applications = new ArrayList<>();

        ofNullable(caseData.getC2DocumentBundle())
            .ifPresent(c2s -> c2s
                .forEach(application -> applications.add(element(application.getId(), application.getValue()))));

        unwrapElements(caseData.getAdditionalApplicationsBundle()).forEach(bundle -> {
            ofNullable(bundle.getC2DocumentBundle())
                .ifPresent(application -> applications.add(element(application.getId(), application)));
            ofNullable(bundle.getOtherApplicationsBundle())
                .ifPresent(application -> applications.add(element(application.getId(), application)));
        });

        ofNullable(caseData.getPlacementEventData())
            .map(PlacementEventData::getPlacements)
            .ifPresent(placement -> placement
                .forEach(application -> applications.add(element(application.getId(), application.getValue()))));

        return applications;
    }

    private String buildMessageHistory(String message, String sender) {
        return buildMessageHistory(message, "", sender);
    }

    private boolean hasC2s(CaseData caseData) {
        return caseData.getC2DocumentBundle() != null;
    }

    private boolean hasAdditionalApplications(CaseData caseData) {
        return ObjectUtils.isNotEmpty(getApplications(caseData));
    }

    private boolean hasPlacementApplications(CaseData caseData) {
        return ObjectUtils.isNotEmpty(caseData.getPlacementEventData().getPlacements());
    }

    private boolean hasSelectedAdditionalApplication(CaseData caseData) {
        return (hasAdditionalApplications(caseData) || hasC2s(caseData) || hasPlacementApplications(caseData))
            && caseData.getMessageJudgeEventData().getAdditionalApplicationsDynamicList() != null;
    }

    private Map<String, Object> prePopulateSenderAndRecipient() {
        Map<String, Object> data = new HashMap<>();

        if (isJudiciary()) {
            data.put("judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(getEmailAddressByRoleType(JudicialMessageRoleType.JUDICIARY))
                .recipient(EMPTY)
                .build());
            data.put("isJudiciary", YES);
        } else {
            data.put("judicialMessageMetaData", JudicialMessageMetaData.builder()
                .sender(EMPTY)
                .recipient(EMPTY)
                .build());
            data.put("isJudiciary", NO);
        }

        return data;
    }

    private SelectableItem getApplication(CaseData caseData, UUID applicationId) {
        return getElement(applicationId, getApplications(caseData)).getValue();
    }

    private DynamicList getApplicationsLists(CaseData caseData, UUID selected) {

        final List<Element<SelectableItem>> applications = getApplications(caseData);

        final Function<Element<SelectableItem>, LocalDateTime> timeExtractor = el ->
            parseLocalDateTimeFromStringUsingFormat(el.getValue().getUploadedDateTime(), DATE_TIME, TIME_DATE);

        final Function<Element<SelectableItem>, Integer> sortOrderExtractor = el -> el.getValue().getSortOrder();

        applications.sort(comparing(sortOrderExtractor).thenComparing(comparing(timeExtractor).reversed()));

        return asDynamicList(applications, selected, SelectableItem::toLabel);
    }

    private List<DocumentReference> getRelatedDocuments(Placement placement) {
        final List<DocumentReference> relatedDocuments = new ArrayList<>();

        relatedDocuments.add(placement.getApplication());

        unwrapElements(placement.getSupportingDocuments()).stream()
            .map(PlacementSupportingDocument::getDocument)
            .collect(toCollection(() -> relatedDocuments));

        unwrapElements(placement.getConfidentialDocuments()).stream()
            .map(PlacementConfidentialDocument::getDocument)
            .collect(toCollection(() -> relatedDocuments));

        if (placement.getPlacementNotice() != null) {
            relatedDocuments.add(placement.getPlacementNotice());
        }

        unwrapElements(placement.getNoticeDocuments()).stream()
            .map(PlacementNoticeDocument::getResponse)
            .filter(Objects::nonNull)
            .collect(toCollection(() -> relatedDocuments));

        return relatedDocuments;
    }

    private String getRelatedDocumentNames(SelectableItem selectedApplication) {
        if (selectedApplication instanceof C2DocumentBundle) {
            return ((C2DocumentBundle) selectedApplication).getAllC2DocumentFileNames();
        }
        if (selectedApplication instanceof Placement) {
            return getRelatedDocumentsNames((Placement) selectedApplication);
        }
        if (selectedApplication instanceof OtherApplicationsBundle) {
            return ((OtherApplicationsBundle) selectedApplication).getAllDocumentFileNames();
        }
        return null;
    }

    private String getRelatedDocumentsNames(Placement placement) {

        final List<String> supportingDocuments = unwrapElements(placement.getSupportingDocuments()).stream()
            .map(PlacementSupportingDocument::getDocument)
            .filter(Objects::nonNull)
            .map(DocumentReference::getFilename)
            .toList();

        final List<String> confidentialDocuments = unwrapElements(placement.getConfidentialDocuments()).stream()
            .map(PlacementConfidentialDocument::getDocument)
            .filter(Objects::nonNull)
            .map(DocumentReference::getFilename)
            .toList();

        final List<String> noticesResponses = unwrapElements(placement.getNoticeDocuments()).stream()
            .map(PlacementNoticeDocument::getResponse)
            .filter(Objects::nonNull)
            .map(DocumentReference::getFilename)
            .toList();

        final StringBuilder fileNamesBuilder = new StringBuilder();

        fileNamesBuilder.append("Application: " + placement.application.getFilename());

        if (ObjectUtils.isNotEmpty(supportingDocuments)) {
            fileNamesBuilder.append("\nSupporting documents: " + join(", ", supportingDocuments));
        }

        if (ObjectUtils.isNotEmpty(confidentialDocuments)) {
            fileNamesBuilder.append("\nConfidential documents: " + join(", ", confidentialDocuments));
        }

        if (placement.getPlacementNotice() != null) {
            fileNamesBuilder.append("\nNotice: " + placement.getPlacementNotice().getFilename());
        }

        if (ObjectUtils.isNotEmpty(noticesResponses)) {
            fileNamesBuilder.append("\nNotice responses: " + join(", ", noticesResponses));
        }

        return fileNamesBuilder.toString();
    }

}
