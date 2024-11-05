package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class SendNewMessageJudgeServiceTest {

    private static final String COURT_EMAIL = "ctsc@test.com";
    private static final String MESSAGE_NOTE = "Message note";
    private static final String MESSAGE_SENDER_JUDICIARY = "judiciary@test.com";
    private static final String MESSAGE_SENDER = "sender@fpla.com";
    private static final String MESSAGE_REQUESTED_BY = "request review from some court";
    private static final String MESSAGE_RECIPIENT = "recipient@fpla.com";
    private static final String C2_FILE_NAME = "c2.doc";
    private static final String C2_SUPPORTING_DOCUMENT_FILE_NAME = "c2_supporting.doc";
    private static final UUID SELECTED_DYNAMIC_LIST_ITEM_ID = randomUUID();
    private static final UUID NEW_ELEMENT_ID = randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE_1 = testDocumentReference("Test Doc One");
    private static final UUID DOCUMENT_1_ID = randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE_2 = testDocumentReference("Test Doc Two");
    private static final UUID DOCUMENT_2_ID = randomUUID();

    @Mock
    private Time time;
    @Mock
    private IdentityService identityService;
    @Mock
    private UserService userService;
    @Mock
    private ManageDocumentService manageDocumentService;
    @Mock
    private CtscEmailLookupConfiguration ctscEmailLookupConfiguration;
    @Spy
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private SendNewMessageJudgeService sendNewMessageJudgeService;

    @BeforeEach
    void init() {
        when(ctscEmailLookupConfiguration.getEmail()).thenReturn(COURT_EMAIL);
        when(time.now()).thenReturn(LocalDateTime.now());
        when(manageDocumentService.buildExistingDocumentTypeDynamicList(any()))
            .thenReturn(buildBasicDocumentTypeDynamicList());
        when(manageDocumentService.buildAvailableDocumentsDynamicList(any()))
            .thenReturn(buildBasicDocumentDynamicList());
        when(manageDocumentService.getSelectedDocuments(any(), any(), any()))
            .thenReturn(List.of(element(SkeletonArgument.builder().document(DOCUMENT_REFERENCE_1).build())));
    }

    @Test
    void shouldPopulateDynamicListWhenAttachingAdditionalApplication() {
        final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .uploadedDateTime("1 December 2020, 12:00pm")
            .author("Some author")
            .build();

        final C2DocumentBundle confC2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .uploadedDateTime("2 December 2020, 12:00pm")
            .author("Some author")
            .build();

        final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(randomUUID(),
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .build()),
            element(randomUUID(), AdditionalApplicationsBundle.builder()
                .c2DocumentBundleConfidential(confC2DocumentBundle)
                .build()));

        final Element<Placement> placement = element(Placement.builder()
            .childName("Alex Green")
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 12, 13, 0))
            .build());

        final CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .placementEventData(PlacementEventData.builder()
                .placements(List.of(placement))
                .build())
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
                .build())
            .build();

        final Map<String, Object> expectedEventData = sendNewMessageJudgeService.populateDynamicLists(caseData);

        final DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(confC2DocumentBundle.getId(), "C2, 2 December 2020, 12:00pm"),
            Pair.of(c2DocumentBundle.getId(), "C2, 1 December 2020, 12:00pm"),
            Pair.of(placement.getId(), "A50, Alex Green, 12 October 2020, 1:00pm"));

        final Map<String, Object> expectedData = Map.of(
            "additionalApplicationsDynamicList", expectedAdditionalApplicationsDynamicList);

        assertThat(expectedEventData).isEqualTo(expectedData);
    }

    @Test
    void shouldPopulateRelatedDocumentsLabelWithAdditionalApplicationSelected() {
        final C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .uploadedDateTime("1 December 2020, 12:00pm")
            .author("Some author")
            .build();

        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = List.of(
            element(SupportingEvidenceBundle.builder().document(DOCUMENT_REFERENCE_1).build()),
            element(SupportingEvidenceBundle.builder().document(DOCUMENT_REFERENCE_2).build())
        );

        final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(randomUUID(),
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2DocumentBundle)
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .supportingEvidenceBundle(supportingEvidenceBundle).build())
                    .build()));

        final Element<Placement> placement = element(Placement.builder()
            .childName("Alex Green")
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 12, 13, 0))
            .build());

        final DynamicList dynamicListWithSelectedValue = buildDynamicList(1,
            Pair.of(c2DocumentBundle.getId(), "C2, 1 December 2020, 12:00pm"));

        final CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .placementEventData(PlacementEventData.builder()
                .placements(List.of(placement))
                .build())
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
                .additionalApplicationsDynamicList(dynamicListWithSelectedValue)
                .build())
            .build();

        final Map<String, Object> expectedEventData = sendNewMessageJudgeService.populateNewMessageFields(caseData);

        final Map<String, Object> expectedData = Map.of(
            "nextHearingLabel", "",
            "relatedDocumentsLabel",
            format("%s\n%s", DOCUMENT_REFERENCE_1.getFilename(), DOCUMENT_REFERENCE_2.getFilename()));

        assertThat(expectedEventData).isEqualTo(expectedData);
    }

    @Test
    void shouldPopulateDynamicListWhenDocument() {
        UUID applicationId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = List.of(element(
            AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(applicationId)
                    .uploadedDateTime("01 Dec 2020")
                    .author("Some author")
                    .build())
                .build())
        );

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION).build())
            .build();

        Map<String, Object> data = sendNewMessageJudgeService.populateDynamicLists(caseData);

        DynamicList expectedAdditionalApplicationsDynamicList = buildDynamicList(
            Pair.of(applicationId, "C2, 01 Dec 2020")
        );

        Map<String, Object> expectedData = Map.of(
            "additionalApplicationsDynamicList", expectedAdditionalApplicationsDynamicList
        );

        assertThat(data).isEqualTo(expectedData);
    }

    @Test
    void shouldNotPopulateApplicationWhenAppsDoNotExist() {
        assertThat(sendNewMessageJudgeService.initialiseCaseFields(CaseData.builder().build()))
            .doesNotContain(entry("hasAdditionalApplications", YesNo.YES));
    }

    @Test
    void shouldPrePopulateSenderAndRecipientEmailsWhenNewMessageIsInitiatedByJudge() {
        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);

        CaseData caseData = CaseData.builder().build();

        assertThat(sendNewMessageJudgeService.initialiseCaseFields(caseData))
            .containsOnly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .recipient(EMPTY)
                    .sender(MESSAGE_SENDER).build()),
                entry("isJudiciary", YesNo.YES),
                entry("documentTypesDynamicList",
                    manageDocumentService.buildExistingDocumentTypeDynamicList(caseData)));
    }

    @Test
    void shouldNotPrePopulateSenderAndRecipientEmailsWhenNewMessageIsInitiatedNotByJudge() {
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(false);

        CaseData caseData = CaseData.builder().build();

        assertThat(sendNewMessageJudgeService.initialiseCaseFields(caseData))
            .containsOnly(
                entry("judicialMessageMetaData", JudicialMessageMetaData.builder()
                    .sender(EMPTY)
                    .recipient(EMPTY).build()),
                entry("isJudiciary", YesNo.NO),
                entry("documentTypesDynamicList", manageDocumentService.buildDocumentTypeDynamicList(caseData)));
    }

    @Test
    void shouldReturnEmptyMapWhenNoAttachmentSelected() {
        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .document(DocumentReference.builder()
                .filename(C2_FILE_NAME)
                .build())
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
                        .build())
                    .build())))
            .build();

        C2DocumentBundle notSelectedC2DocumentBundle = C2DocumentBundle.builder()
            .id(randomUUID())
            .document(DocumentReference.builder()
                .filename("other_c2.doc")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationsBundle(
                wrapElements(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(selectedC2DocumentBundle)
                        .build(),
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(notSelectedC2DocumentBundle)
                        .build()
                )
            )
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.NONE)
                .build())
            .build();

        assertThat(sendNewMessageJudgeService.populateNewMessageFields(caseData)).containsOnlyKeys("nextHearingLabel");
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenDocumentNotSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .subject(MESSAGE_REQUESTED_BY)
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .urgency("High urgency")
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.NONE)
            .judicialMessageMetaData(judicialMessageMetaData)
            .build();

        CaseData caseData = CaseData.builder().messageJudgeEventData(messageJudgeEventData).build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);
        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = sendNewMessageJudgeService.addNewJudicialMessage(caseData);

        Element<JudicialMessage> expectedJudicialMessageElement = element(NEW_ELEMENT_ID, JudicialMessage.builder()
            .updatedTime(time.now())
            .status(OPEN)
            .latestMessage(MESSAGE_NOTE)
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .sender(MESSAGE_SENDER)
            .recipient(MESSAGE_RECIPIENT)
            .subject(MESSAGE_REQUESTED_BY)
            .urgency("High urgency")
            .messageHistory(format("%s - %s", MESSAGE_SENDER, MESSAGE_NOTE))
            .build());

        assertThat(updatedMessages).hasSize(1).first().isEqualTo(expectedJudicialMessageElement);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenAdditionalApplicationDocumentHasBeenSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .recipient(MESSAGE_RECIPIENT)
            .build();

        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(C2_FILE_NAME)
            .build();

        DocumentReference supportingC2DocumentReference = DocumentReference.builder()
            .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
            .build();

        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2DocumentReference)
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
            .judicialMessageMetaData(judicialMessageMetaData)
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2DocumentBundle)
                .build()))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = sendNewMessageJudgeService.addNewJudicialMessage(caseData);
        JudicialMessage newMessage = updatedMessages.get(0).getValue();
        List<Element<DocumentReference>> relatedDocuments = newMessage.getRelatedDocuments();

        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            selectedC2DocumentBundle.getAllC2DocumentFileNames()
        );
        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenOtherApplicationDocumentHasBeenSelected() {
        JudicialMessageMetaData judicialMessageMetaData = JudicialMessageMetaData.builder()
            .recipient(MESSAGE_RECIPIENT)
            .build();

        DocumentReference mainC2DocumentReference = DocumentReference.builder()
            .filename(C2_FILE_NAME)
            .build();

        DocumentReference supportingC2DocumentReference = DocumentReference.builder()
            .filename(C2_SUPPORTING_DOCUMENT_FILE_NAME)
            .build();

        OtherApplicationsBundle selectedOtherApplicationBundle = OtherApplicationsBundle.builder()
            .id(SELECTED_DYNAMIC_LIST_ITEM_ID)
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .document(mainC2DocumentReference)
            .supportingEvidenceBundle(List.of(
                element(SupportingEvidenceBundle.builder()
                    .document(supportingC2DocumentReference)
                    .build())))
            .build();

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
            .judicialMessageMetaData(judicialMessageMetaData)
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(SELECTED_DYNAMIC_LIST_ITEM_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .otherApplicationsBundle(selectedOtherApplicationBundle)
                .build()))
            .build();

        when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);

        List<Element<JudicialMessage>> updatedMessages = sendNewMessageJudgeService.addNewJudicialMessage(caseData);
        JudicialMessage newMessage = updatedMessages.get(0).getValue();
        List<Element<DocumentReference>> relatedDocuments = newMessage.getRelatedDocuments();

        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            selectedOtherApplicationBundle.getAllDocumentFileNames()
        );
        assertThat(relatedDocuments.get(0).getValue()).isEqualTo(mainC2DocumentReference);
        assertThat(relatedDocuments.get(1).getValue()).isEqualTo(supportingC2DocumentReference);
    }

    @Test
    void shouldAppendNewJudicialMessageToJudicialMessageListWhenPlacementApplicationHasBeenSelected() {

        final DocumentReference placementApplication = testDocumentReference("placement application");
        final DocumentReference supportingDocument1 = testDocumentReference("first supporting document");
        final DocumentReference supportingDocument2 = testDocumentReference("second supporting document");
        final DocumentReference confidentialDocument1 = testDocumentReference("first confidential document");
        final DocumentReference notice1 = testDocumentReference("first notice document");
        final DocumentReference notice1Response = testDocumentReference("first notice response");

        final Element<Placement> placement = element(Placement.builder()
            .childName("Alex Green")
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 12, 13, 0))
            .application(placementApplication)
            .placementNotice(notice1)
            .supportingDocuments(wrapElements(
                PlacementSupportingDocument.builder()
                    .document(supportingDocument1)
                    .build(),
                PlacementSupportingDocument.builder()
                    .document(supportingDocument2)
                    .build()))
            .confidentialDocuments(wrapElements(
                PlacementConfidentialDocument.builder()
                    .document(confidentialDocument1)
                    .build()))
            .noticeDocuments(wrapElements(
                PlacementNoticeDocument.builder()
                    .response(notice1Response)
                    .build()))
            .build());

        final MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .recipient(MESSAGE_RECIPIENT)
                .build())
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(placement.getId())
                    .build())
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .placementEventData(PlacementEventData.builder()
                .placements(List.of(placement))
                .build())
            .build();

        final List<Element<JudicialMessage>> updatedMessages =
            sendNewMessageJudgeService.addNewJudicialMessage(caseData);

        final JudicialMessage newMessage = updatedMessages.get(0).getValue();

        assertThat(newMessage.getRelatedDocumentFileNames()).isEqualTo(
            "Application: placement application\n"
                + "Supporting documents: first supporting document, second supporting document\n"
                + "Confidential documents: first confidential document\n"
                + "Notice: first notice document\n"
                + "Notice responses: first notice response");

        assertThat(newMessage.getRelatedDocuments()).extracting(Element::getValue).containsExactly(
            placementApplication,
            supportingDocument1,
            supportingDocument2,
            confidentialDocument1,
            notice1,
            notice1Response);
    }

    @Test
    void shouldAppendJudicialMessageToJudicialMessageListWhenPlacementApplicationWithMissingDocumentsHasBeenSelected() {

        final DocumentReference placementApplication = testDocumentReference("placement application");

        final Element<Placement> placement = element(Placement.builder()
            .childName("Alex Green")
            .placementUploadDateTime(LocalDateTime.of(2020, 10, 12, 13, 0))
            .application(placementApplication)
            .build());

        final MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .recipient(MESSAGE_RECIPIENT)
                .build())
            .additionalApplicationsDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(placement.getId())
                    .build())
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .placementEventData(PlacementEventData.builder()
                .placements(List.of(placement))
                .build())
            .build();

        final List<Element<JudicialMessage>> updatedMessages =
            sendNewMessageJudgeService.addNewJudicialMessage(caseData);

        final JudicialMessage newMessage = updatedMessages.get(0).getValue();

        assertThat(newMessage.getRelatedDocumentFileNames())
            .isEqualTo("Application: placement application");

        assertThat(newMessage.getRelatedDocuments())
            .extracting(Element::getValue)
            .containsExactly(placementApplication);
    }

    @Test
    void shouldAppendJudicialMessageToJudicialMessageListWhenSkeletonArgumentHasBeenSelected() {

        final SkeletonArgument skeletonArgument = SkeletonArgument.builder()
            .document(DOCUMENT_REFERENCE_1)
            .build();

        final HearingCourtBundle courtBundle = HearingCourtBundle.builder()
            .courtBundle(List.of(element(DOCUMENT_2_ID, CourtBundle.builder()
                .document(DOCUMENT_REFERENCE_2)
                .build())))
            .build();

        final MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .isMessageRegardingDocuments(MessageRegardingDocuments.DOCUMENT)
            .judicialMessageMetaData(JudicialMessageMetaData.builder()
                .recipient(MESSAGE_RECIPIENT)
                .build())
            .documentDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(format("hearingDocuments.skeletonArgumentList###%s", DOCUMENT_1_ID))
                    .build())
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .hearingDocuments(HearingDocuments.builder()
                .skeletonArgumentList(List.of(element(DOCUMENT_1_ID, skeletonArgument)))
                .courtBundleListV2(List.of(element(courtBundle))).build())
            .build();

        final List<Element<JudicialMessage>> updatedMessages =
            sendNewMessageJudgeService.addNewJudicialMessage(caseData);

        final JudicialMessage newMessage = updatedMessages.get(0).getValue();

        assertThat(newMessage.getRelatedDocumentFileNames())
            .isEqualTo("Test Doc One");

        assertThat(newMessage.getRelatedDocuments())
            .extracting(Element::getValue)
            .containsExactly(DOCUMENT_REFERENCE_1);
    }

    private static Stream<Arguments> argForShouldAppendNewJudicialMessageToExistingJudicialMessageList() {
        List<Arguments> args = new ArrayList<>();
        List<JudicialMessageRoleType> judicialMessageRoleTypes =
            List.of(JudicialMessageRoleType.JUDICIARY, JudicialMessageRoleType.CTSC,
                JudicialMessageRoleType.LOCAL_COURT_ADMIN,JudicialMessageRoleType.OTHER);

        List.of(true, false).stream()
            .forEach(isJuducuary -> {
                judicialMessageRoleTypes.stream().forEach(senderRole -> {
                    judicialMessageRoleTypes.stream().forEach(recipientRole -> {
                        args.add(Arguments.of(senderRole, recipientRole, isJuducuary));
                    });
                });
            });
        return args.stream();
    }

    @ParameterizedTest
    @MethodSource("argForShouldAppendNewJudicialMessageToExistingJudicialMessageList")
    void shouldAppendNewJudicialMessageToExistingJudicialMessageList(JudicialMessageRoleType senderRole,
                                                                     JudicialMessageRoleType recipientRole,
                                                                     boolean isJudiciary) {
        if (isJudiciary) {
            when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);
            when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER_JUDICIARY);
        } else {
            when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(false);
            when(userService.getUserEmail()).thenReturn(MESSAGE_SENDER);
        }

        JudicialMessage newMessage = JudicialMessage.builder()
            .subject(MESSAGE_REQUESTED_BY)
            .recipientType(recipientRole)
            .recipient(MESSAGE_RECIPIENT)
            .senderType(senderRole)
            .sender(MESSAGE_SENDER)
            .build();

        UUID existingJudicialMessageId = randomUUID();
        JudicialMessage existingJudicialMessage = JudicialMessage.builder().build();

        List<Element<JudicialMessage>> existingJudicialMessages = new ArrayList<>();
        existingJudicialMessages.add(element(existingJudicialMessageId, existingJudicialMessage));

        MessageJudgeEventData messageJudgeEventData = MessageJudgeEventData.builder()
            .judicialMessageNote(MESSAGE_NOTE)
            .judicialMessageMetaData(newMessage)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(messageJudgeEventData)
            .judicialMessages(existingJudicialMessages)
            .build();

        JudicialMessageRoleType expectedSenderRole = (isJudiciary) ? JudicialMessageRoleType.JUDICIARY : senderRole;
        String expectedSender = MESSAGE_SENDER;
        if (JudicialMessageRoleType.CTSC.equals(expectedSenderRole)) {
            expectedSender = COURT_EMAIL;
        } else if (isJudiciary) {
            expectedSender = MESSAGE_SENDER_JUDICIARY;
        }

        String expectedRecipient = (JudicialMessageRoleType.CTSC.equals(recipientRole))
            ? COURT_EMAIL : MESSAGE_RECIPIENT;

        JudicialMessage expectedNewJudicialMessage = JudicialMessage.builder()
            .senderType(expectedSenderRole)
            .sender(expectedSender)
            .recipientType(recipientRole)
            .recipient(expectedRecipient)
            .updatedTime(time.now())
            .status(OPEN)
            .subject(MESSAGE_REQUESTED_BY)
            .latestMessage(MESSAGE_NOTE)
            .messageHistory(format("%s - %s", expectedSender, MESSAGE_NOTE))
            .dateSent(formatLocalDateTimeBaseUsingFormat(time.now(), DATE_TIME_AT))
            .build();

        when(identityService.generateId()).thenReturn(NEW_ELEMENT_ID);

        List<Element<JudicialMessage>> updatedMessages = sendNewMessageJudgeService.addNewJudicialMessage(caseData);

        assertThat(updatedMessages.size()).isEqualTo(2);
        assertThat(updatedMessages).isEqualTo(List.of(
            element(existingJudicialMessageId, existingJudicialMessage),
            element(NEW_ELEMENT_ID, expectedNewJudicialMessage)
        ));
    }

    @Test
    void shouldSortThreadOfJudicialMessagesByDate() {
        Element<JudicialMessage> latestJudicialMessage = buildJudicialMessageElement(time.now().plusDays(1), OPEN);
        Element<JudicialMessage> pastJudicialMessage = buildJudicialMessageElement(time.now().plusMinutes(1), OPEN);
        Element<JudicialMessage> oldestJudicialMessage = buildJudicialMessageElement(time.now().minusHours(1), OPEN);

        List<Element<JudicialMessage>> judicialMessages = new ArrayList<>();
        judicialMessages.add(oldestJudicialMessage);
        judicialMessages.add(latestJudicialMessage);
        judicialMessages.add(pastJudicialMessage);

        List<Element<JudicialMessage>> sortedJudicialMessages
            = sendNewMessageJudgeService.sortJudicialMessages(judicialMessages);

        assertThat(sortedJudicialMessages).isEqualTo(List.of(latestJudicialMessage, pastJudicialMessage,
            oldestJudicialMessage));
    }

    @Test
    void shouldPopulateFirstHearingLabelWhenHearingExists() {
        HearingType hearingType = CASE_MANAGEMENT;
        LocalDateTime hearingStartDate = LocalDateTime.now();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder()
                .type(hearingType)
                .startDate(hearingStartDate)
                .build())))
            .build();

        assertThat(sendNewMessageJudgeService.getNextHearingLabel(caseData))
            .isEqualTo(format("Next hearing in the case: %s hearing, %s", hearingType.getLabel(),
                formatLocalDateTimeBaseUsingFormat(hearingStartDate, DATE)));
    }

    @Test
    void shouldNotPopulateFirstHearingLabelWhenHearingDoesNotExists() {
        CaseData caseData = CaseData.builder().build();

        assertThat(sendNewMessageJudgeService.getNextHearingLabel(caseData)).isEmpty();
    }

    @Test
    void shouldShowErrorWhenNoDocumentOfSelectedType() {
        DynamicListElement skeletonArgumentElement = DynamicListElement.builder()
            .code("SKELETON_ARGUMENTS")
            .label("Skeleton arguments")
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.DOCUMENT)
                .documentTypesDynamicList(DynamicList.builder().value(skeletonArgumentElement).build())
                .build())
            .build();

        when(sendNewMessageJudgeService.getDocumentList(caseData))
            .thenReturn(DynamicList.builder().listItems(List.of()).build());

        List<String> expectedError = List.of("No documents available of type: Skeleton arguments");

        assertThat(sendNewMessageJudgeService.validateDynamicLists(caseData)).isEqualTo(expectedError);
    }

    private Element<JudicialMessage> buildJudicialMessageElement(LocalDateTime dateTime, JudicialMessageStatus status) {
        return element(JudicialMessage.builder().updatedTime(dateTime).status(status).build());
    }

    private DynamicList buildBasicDocumentTypeDynamicList() {
        DynamicListElement skeletonArgumentElement = DynamicListElement.builder()
            .code("SKELETON_ARGUMENTS")
            .label("Skeleton arguments")
            .build();

        DynamicListElement courtBundleElement = DynamicListElement.builder()
            .code("COURT_BUNDLE")
            .label("Court Bundle")
            .build();

        return DynamicList.builder()
            .listItems(List.of(skeletonArgumentElement, courtBundleElement))
            .build();
    }

    private DynamicList buildBasicDocumentDynamicList() {
        DynamicListElement skeletonArgumentElement = DynamicListElement.builder()
            .code(format("hearingDocuments.skeletonArgumentList###%s", DOCUMENT_1_ID))
            .label(DOCUMENT_REFERENCE_1.getFilename())
            .build();

        DynamicListElement courtBundleElement = DynamicListElement.builder()
            .code(format("hearingDocuments.courtBundleListV2###%s", DOCUMENT_2_ID))
            .label(DOCUMENT_REFERENCE_2.getFilename())
            .build();

        return DynamicList.builder()
            .listItems(List.of(skeletonArgumentElement, courtBundleElement))
            .build();
    }
}
