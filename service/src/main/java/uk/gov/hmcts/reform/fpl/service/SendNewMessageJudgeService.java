package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.am.model.RoleAssignment;
import uk.gov.hmcts.reform.fpl.enums.JudgeCaseRole;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.LegalAdviserRole;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.SelectableItem;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments.APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments.DOCUMENT;
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
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendNewMessageJudgeService extends MessageJudgeService {

    private final ValidateEmailService validateEmailService;
    private final IdentityService identityService;
    private final ObjectMapper mapper;
    private final DynamicListService dynamicListService;
    private final RoleAssignmentService roleAssignmentService;
    private final JudicialService judicialService;

    public Map<String, Object> initialiseCaseFields(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        if (hasAdditionalApplications(caseData) || hasC2s(caseData)) {
            data.put("hasAdditionalApplications", YES.getValue());
        }

        data.putAll(prePopulateSenderAndRecipient(caseData));
        data.put("documentTypesDynamicList", manageDocumentService.buildExistingDocumentTypeDynamicList(caseData));

        return data;
    }

    public Map<String, Object> populateDynamicLists(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        MessageRegardingDocuments type = getMessageAttachmentType(caseData);

        if (type == DOCUMENT) {
            data.put("documentDynamicList", getDocumentList(caseData));
        } else if (type == APPLICATION) {
            data.put("additionalApplicationsDynamicList", getApplicationsLists(caseData));
        }

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
        } else if (hasSelectedDocument(caseData)) {
            final String selectedDocumentLabel =
                caseData.getMessageJudgeEventData().getDocumentDynamicList().getValueLabel();

            data.put("relatedDocumentsLabel", selectedDocumentLabel);
        }

        data.put("nextHearingLabel", getNextHearingLabel(caseData));

        return data;
    }

    public List<String> validateDynamicLists(CaseData caseData) {
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();

        if (messageJudgeEventData.getIsMessageRegardingDocuments().equals(DOCUMENT)
            && getDocumentList(caseData).getListItems().isEmpty()) {
            return List.of(format("No documents available of type: %s",
                messageJudgeEventData.getDocumentTypesDynamicList().getValue().getLabel()));
        } else if (messageJudgeEventData.getIsMessageRegardingDocuments().equals(APPLICATION)
            && getApplicationsLists(caseData).getListItems().isEmpty()) {
            return List.of("No applications available");
        }

        return Collections.emptyList();
    }

    public List<Element<JudicialMessage>> addNewJudicialMessage(CaseData caseData) {
        List<Element<JudicialMessage>> judicialMessages = caseData.getJudicialMessages();
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        JudicialMessageMetaData judicialMessageMetaData = messageJudgeEventData.getJudicialMessageMetaData();
        String latestMessage = messageJudgeEventData.getJudicialMessageNote();

        // identify roleType and email based off logged-in user org/case roles
        JudicialMessageRoleType roleType = getSenderRole(caseData);
        String senderEmail = getEmailAddressByRoleType(roleType);

        JudicialMessageRoleType recipientRoleType = JudicialMessageRoleType.valueOf(
            messageJudgeEventData.getJudicialMessageMetaData().getRecipientDynamicList().getValue().getCode());

        JudicialMessage.JudicialMessageBuilder<?, ?> judicialMessageBuilder = JudicialMessage.builder()
            .sender(senderEmail)
            .senderType(roleType)
            //.recipient(resolveRecipientEmailAddress(recipientRoleType, judicialMessageMetaData.getRecipient())) no longer needed
            .recipientType(recipientRoleType)
            .subject(judicialMessageMetaData.getSubject())
            .latestMessage(latestMessage)
            .messageHistory(buildMessageHistory(latestMessage, "%s (%s)".formatted(roleType.getLabel(), senderEmail)))
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
        } else if (hasSelectedDocument(caseData)) {
            Optional<DocumentReference> relatedDocument = getSelectedDocumentReference(caseData);
            if (relatedDocument.isPresent()) {
                judicialMessageBuilder.relatedDocuments(wrapElements(relatedDocument.get()));
                judicialMessageBuilder.relatedDocumentFileNames(relatedDocument.get().getFilename());
            }
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
            ofNullable(bundle.getC2DocumentBundleConfidential())
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
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();

        return (hasAdditionalApplications(caseData) || hasC2s(caseData) || hasPlacementApplications(caseData))
            && messageJudgeEventData.getAdditionalApplicationsDynamicList() != null
            && messageJudgeEventData.getIsMessageRegardingDocuments().equals(APPLICATION);
    }

    private boolean hasSelectedDocument(CaseData caseData) {
        MessageJudgeEventData messageJudgeEventData = caseData.getMessageJudgeEventData();
        return messageJudgeEventData.getDocumentDynamicList() != null
            && messageJudgeEventData.getIsMessageRegardingDocuments().equals(DOCUMENT);
    }

    private MessageRegardingDocuments getMessageAttachmentType(CaseData caseData) {
        return caseData.getMessageJudgeEventData().getIsMessageRegardingDocuments();
    }

    private Map<String, Object> prePopulateSenderAndRecipient(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        JudicialMessageRoleType senderRole = getSenderRole(caseData);
        List<RoleAssignment> currentRoles = roleAssignmentService
            .getJudicialCaseRolesAtTime(caseData.getId(), ZonedDateTime.now());

        boolean hasAllocatedJudgeRole = currentRoles.stream()
            .anyMatch(role -> role.getRoleName().equals(JudgeCaseRole.ALLOCATED_JUDGE.getRoleName())
                || role.getRoleName().equals(LegalAdviserRole.ALLOCATED_LEGAL_ADVISER.getRoleName()));

        boolean hasHearingJudgeRole = currentRoles.stream()
            .anyMatch(role -> role.getRoleName().equals(JudgeCaseRole.HEARING_JUDGE.getRoleName())
                || role.getRoleName().equals(LegalAdviserRole.HEARING_LEGAL_ADVISER.getRoleName()));


        Optional<Judge> allocatedJudge = judicialService.getAllocatedJudge(caseData);
        Optional<JudgeAndLegalAdvisor> hearingJudge = judicialService.getCurrentHearingJudge(caseData);

        List<DynamicListElement> elements = new ArrayList<>();

        elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.CTSC.toString())
            .label(JudicialMessageRoleType.CTSC.getLabel())
            .build());
        elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.LOCAL_COURT_ADMIN.toString())
            .label(JudicialMessageRoleType.LOCAL_COURT_ADMIN.getLabel()
                + " - %s".formatted(caseData.getCourt().getName())
            )
            .build());

        allocatedJudge.ifPresent(judge -> elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.ALLOCATED_JUDGE.toString())
            .label("Allocated %s - %s".formatted(
                    (JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(judge.getJudgeTitle()) ? "Legal Adviser" : "Judge"),
                    formatJudgeTitleAndName(judge.toJudgeAndLegalAdvisor()))
                + (hasAllocatedJudgeRole ? "" : " (missing active role assignment)")
            )
            .build()));

        hearingJudge.ifPresent(judge -> elements.add(DynamicListElement.builder()
            .code(JudicialMessageRoleType.HEARING_JUDGE.toString())
            .label("Hearing %s - %s".formatted(
                    (JudgeOrMagistrateTitle.LEGAL_ADVISOR.equals(judge.getJudgeTitle()) ? "Legal Adviser" : "Judge"),
                    formatJudgeTitleAndName(judge))
                + (hasHearingJudgeRole ? "" : " (missing active role assignment)")
            )
            .build()));

        DynamicList dyList = DynamicList.builder()
            .listItems(elements.stream()
                .filter(el -> !el.getCode().equals(senderRole.toString()))
                .toList())
            .build();

        data.put("judicialMessageMetaData", JudicialMessageMetaData.builder()
            .recipientDynamicList(dyList)
            .build());
        data.put("isJudiciary", NO);

        return data;
    }

    private SelectableItem getApplication(CaseData caseData, UUID applicationId) {
        return getElement(applicationId, getApplications(caseData)).getValue();
    }

    private DynamicList getApplicationsLists(CaseData caseData) {

        final List<Element<SelectableItem>> applications = getApplications(caseData);

        final Function<Element<SelectableItem>, LocalDateTime> timeExtractor = el ->
            parseLocalDateTimeFromStringUsingFormat(el.getValue().getUploadedDateTime(), DATE_TIME, TIME_DATE);

        final Function<Element<SelectableItem>, Integer> sortOrderExtractor = el -> el.getValue().getSortOrder();

        applications.sort(comparing(sortOrderExtractor).thenComparing(comparing(timeExtractor).reversed()));

        return asDynamicList(applications, SelectableItem::toLabel);
    }

    public DynamicList getDocumentList(CaseData caseData) {
        DocumentType documentTypeSelected = DocumentType.valueOf(caseData.getMessageJudgeEventData()
            .getDocumentTypesDynamicList().getValue().getCode());

        return manageDocumentService
            .buildAvailableDocumentsDynamicList(caseData, documentTypeSelected);
    }

    private Optional<DocumentReference> getSelectedDocumentReference(CaseData caseData) {
        DynamicListElement selected = caseData.getMessageJudgeEventData().getDocumentDynamicList().getValue();

        List<Element<? extends WithDocument>> targetElements = manageDocumentService.getSelectedDocuments(
            caseData, selected, Optional.empty());

        return targetElements.stream().findFirst()
            .map(Element::getValue)
            .map(WithDocument::getDocument);
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
            .collect(toList());

        final List<String> confidentialDocuments = unwrapElements(placement.getConfidentialDocuments()).stream()
            .map(PlacementConfidentialDocument::getDocument)
            .filter(Objects::nonNull)
            .map(DocumentReference::getFilename)
            .collect(toList());

        final List<String> noticesResponses = unwrapElements(placement.getNoticeDocuments()).stream()
            .map(PlacementNoticeDocument::getResponse)
            .filter(Objects::nonNull)
            .map(DocumentReference::getFilename)
            .collect(toList());

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
