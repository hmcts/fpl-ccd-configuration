package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.PRIVATE_SOLICITOR_USER_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FAMILY_MAN_NUMBER;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntilSecs;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentedRespondentWithAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondentWithAddress;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersForPlacementOrderSubmittedControllerTest extends AbstractCallbackTest {

    private static final Order ORDER = A70_PLACEMENT_ORDER;

    private static final DocumentReference ORDER_DOCUMENT_REFERENCE = testDocumentReference();
    private static final byte[] ORDER_BINARY = testDocumentBinaries();

    private static final DocumentReference ORDER_NOTIFICATION_DOCUMENT_REFERENCE = testDocumentReference();
    private static final Document ORDER_NOTIFICATION_DOCUMENT = testDocument();
    private static final byte[] ORDER_NOTIFICATION_BINARY = testDocumentBinaries();

    private static final Document FATHER_COVERSHEET_DOCUMENT = testDocument();
    private static final byte[] FATHER_COVERSHEET_BINARY = testDocumentBinaries();
    private static final String FATHER_SOLICITOR_EMAIL = "father-solicitor@example.com";

    private static final Document MOTHER_COVERSHEET_DOCUMENT = testDocument();
    private static final byte[] MOTHER_COVERSHEET_BINARY = testDocumentBinaries();
    private static final String MOTHER_SOLICITOR_EMAIL = "mother-solicitor@example.com";

    private static final String NOTIFICATION_REFERENCE = "localhost/" + TEST_CASE_ID;

    private static final Element<Respondent> ANOTHER_RESPONDENT = element(Respondent.builder().build());

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> letterCaptor;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CafcassNotificationService cafcassNotificationService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataDelta;

    ManageOrdersForPlacementOrderSubmittedControllerTest() {
        super("manage-orders");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
        when(featureToggleService.isWATaskEmailsEnabled()).thenReturn(true);
    }

    @Test
    void shouldSendPlacementOrderAndOrderNotificationToExpectedPartiesWhenChildAndParentsAreNotRepresented() {
        Element<Respondent> father = testRespondentWithAddress("Father", "Jones");
        Element<Respondent> mother = testRespondentWithAddress("Mother", "Jones");

        when(documentDownloadService.downloadDocument(ORDER_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_BINARY);
        when(documentDownloadService.downloadDocument(ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_NOTIFICATION_BINARY);
        when(documentConversionService.convertToPdf(ORDER_DOCUMENT_REFERENCE))
            .thenReturn(ORDER_DOCUMENT_REFERENCE);
        when(documentConversionService.convertToPdfBytes(ORDER_DOCUMENT_REFERENCE))
            .thenReturn(ORDER_BINARY);
        when(documentConversionService.convertToPdf(ORDER_NOTIFICATION_DOCUMENT_REFERENCE))
            .thenReturn(ORDER_NOTIFICATION_DOCUMENT_REFERENCE);
        when(documentConversionService.convertToPdfBytes(ORDER_NOTIFICATION_DOCUMENT_REFERENCE))
            .thenReturn(ORDER_NOTIFICATION_BINARY);
        when(uploadDocumentService.uploadPDF(ORDER_NOTIFICATION_BINARY,
            ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getFilename() + ".pdf"))
            .thenReturn(ORDER_NOTIFICATION_DOCUMENT);
        when(uploadDocumentService.uploadPDF(FATHER_COVERSHEET_BINARY, COVERSHEET_PDF))
            .thenReturn(FATHER_COVERSHEET_DOCUMENT);
        when(uploadDocumentService.uploadPDF(MOTHER_COVERSHEET_BINARY, COVERSHEET_PDF))
            .thenReturn(MOTHER_COVERSHEET_DOCUMENT);
        when(documentService.createCoverDocuments(TEST_FAMILY_MAN_NUMBER,
            TEST_CASE_ID,
            father.getValue().getParty(),
            ENGLISH)).thenReturn(DocmosisDocument.builder().bytes(FATHER_COVERSHEET_BINARY).build());
        when(documentService.createCoverDocuments(TEST_FAMILY_MAN_NUMBER,
            TEST_CASE_ID,
            mother.getValue().getParty(),
            ENGLISH)).thenReturn(DocmosisDocument.builder().bytes(MOTHER_COVERSHEET_BINARY).build());

        UUID firstLetterId = UUID.randomUUID();
        UUID secondLetterId = UUID.randomUUID();
        when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(firstLetterId), new SendLetterResponse(secondLetterId));

        Element<Child> child = element(Child.builder()
            .party(ChildParty.builder().firstName("Theodore").lastName("Bailey").build())
            .build());
        CaseData placementOrderCaseData = buildCaseData(child, father, mother);

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(placementOrderCaseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(toCallBackRequest(placementOrderCaseData, CaseData.builder().build()));

        //Order
        checkPlacementOrderWasDeliveredAsExpected();
        //Order notification
        checkOrderNotificationLetterWasMailedToParents(father.getValue(), mother.getValue());
        checkCaseDataHasReferenceToSentLetters(firstLetterId, secondLetterId, father, mother);

        verify(cafcassNotificationService).sendEmail(any(),
            eq(Set.of(ORDER_DOCUMENT_REFERENCE, ORDER_NOTIFICATION_DOCUMENT_REFERENCE)),
            eq(CafcassRequestEmailContentProvider.ORDER),
            eq(OrderCafcassData.builder().documentName(ORDER_DOCUMENT_REFERENCE.getFilename()).build()));

        verify(concurrencyHelper, times(2)).startEvent(any(), any());
        verify(concurrencyHelper, times(2)).submitEvent(any(), any(), any());
    }

    @Test
    void shouldSendPlacementOrderDocumentAndNotificationDocumentToExpectedParties_WhenChildAndParentsAreRepresented() {
        Element<Respondent> father = testRepresentedRespondentWithAddress("Father", "Jones", FATHER_SOLICITOR_EMAIL);
        Element<Respondent> mother = testRepresentedRespondentWithAddress("Mother", "Jones", MOTHER_SOLICITOR_EMAIL);
        when(documentDownloadService.downloadDocument(ORDER_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_BINARY);
        when(documentDownloadService.downloadDocument(ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_NOTIFICATION_BINARY);
        when(uploadDocumentService.uploadPDF(ORDER_NOTIFICATION_BINARY,
            ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getFilename())).thenReturn(ORDER_NOTIFICATION_DOCUMENT);
        when(uploadDocumentService.uploadPDF(FATHER_COVERSHEET_BINARY,
            COVERSHEET_PDF)).thenReturn(FATHER_COVERSHEET_DOCUMENT);
        when(uploadDocumentService.uploadPDF(MOTHER_COVERSHEET_BINARY,
            COVERSHEET_PDF)).thenReturn(MOTHER_COVERSHEET_DOCUMENT);
        when(documentService.createCoverDocuments(TEST_FAMILY_MAN_NUMBER,
            TEST_CASE_ID,
            father.getValue().getParty(),
            ENGLISH)).thenReturn(DocmosisDocument.builder().bytes(FATHER_COVERSHEET_BINARY).build());
        when(documentService.createCoverDocuments(TEST_FAMILY_MAN_NUMBER,
            TEST_CASE_ID,
            mother.getValue().getParty(),
            ENGLISH)).thenReturn(DocmosisDocument.builder().bytes(MOTHER_COVERSHEET_BINARY).build());

        Element<Child> child = element(Child.builder()
            .party(ChildParty.builder().firstName("Theodore").lastName("Bailey").build())
            .solicitor(RespondentSolicitor.builder().email(PRIVATE_SOLICITOR_USER_EMAIL).build())
            .build());

        CaseData placementOrderCaseData = buildCaseData(child, father, mother);
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(placementOrderCaseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(toCallBackRequest(placementOrderCaseData, CaseData.builder().build()));

        //Order
        checkPlacementOrderWasDeliveredAsExpected();
        //Order notification
        checkEmailWithOrderNotificationWasSentToParentsSolicitors();
        checkEmailWithOrderNotificationWasSentToChildSolicitor();

        verify(cafcassNotificationService).sendEmail(any(),
            eq(Set.of(ORDER_DOCUMENT_REFERENCE, ORDER_NOTIFICATION_DOCUMENT_REFERENCE)),
            eq(CafcassRequestEmailContentProvider.ORDER),
            eq(OrderCafcassData.builder().documentName(ORDER_DOCUMENT_REFERENCE.getFilename()).build()));

        verify(concurrencyHelper, times(1)).startEvent(any(), any());
        verify(concurrencyHelper, times(1)).submitEvent(any(), any(), any());

    }

    private CaseData buildCaseData(Element<Child> child,
                                   Element<Respondent> firstParent,
                                   Element<Respondent> secondParent) {
        return CaseData.builder()
            .id(TEST_CASE_ID)
            .familyManCaseNumber(TEST_FAMILY_MAN_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .respondents1(List.of(ANOTHER_RESPONDENT, firstParent, secondParent))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder()
                        .childId(child.getId())
                        .placementRespondentsToNotify(List.of(firstParent, secondParent))
                        .build()
                )).build())
            .orderCollection(wrapElements(
                GeneratedOrder.builder()
                    .orderType(ORDER.name())
                    .type(ORDER.getTitle())
                    .document(ORDER_DOCUMENT_REFERENCE)
                    .notificationDocument(ORDER_NOTIFICATION_DOCUMENT_REFERENCE)
                    .children(List.of(child))
                    .build()
            )).build();
    }

    private void checkPlacementOrderWasDeliveredAsExpected() {
        checkEmailWithOrderWasSent(LOCAL_AUTHORITY_1_INBOX);
        checkEmailWithOrderWasSent(DEFAULT_ADMIN_EMAIL);
    }

    private void checkEmailWithOrderWasSent(String recipient) {
        checkEmailWasDelivered(recipient, ORDER_BINARY);
    }

    private void checkEmailWasDelivered(String recipient, byte[] attachedFile) {
        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(PLACEMENT_ORDER_GENERATED_NOTIFICATION_TEMPLATE),
            eq(recipient),
            eq(getExpectedEmailParameters(attachedFile)),
            eq(NOTIFICATION_REFERENCE)
        ));
    }

    private void checkEmailWithOrderNotificationWasSentToParentsSolicitors() {
        checkEmailWasDelivered(FATHER_SOLICITOR_EMAIL, ORDER_NOTIFICATION_BINARY);
        checkEmailWasDelivered(MOTHER_SOLICITOR_EMAIL, ORDER_NOTIFICATION_BINARY);
    }

    private void checkEmailWithOrderNotificationWasSentToChildSolicitor() {
        checkEmailWasDelivered(PRIVATE_SOLICITOR_USER_EMAIL, ORDER_NOTIFICATION_BINARY);
    }

    private void checkOrderNotificationLetterWasMailedToParents(Respondent father, Respondent mother) {
        checkUntil(() -> verify(sendLetterApi, times(2)).sendLetter(
            eq(SERVICE_AUTH_TOKEN),
            letterCaptor.capture()
        ));
        assertThat(letterCaptor.getAllValues()).usingRecursiveComparison().isEqualTo(List.of(
            printRequest(TEST_CASE_ID, ORDER_NOTIFICATION_DOCUMENT_REFERENCE, father.getParty(),
                FATHER_COVERSHEET_BINARY, ORDER_NOTIFICATION_BINARY),
            printRequest(TEST_CASE_ID, ORDER_NOTIFICATION_DOCUMENT_REFERENCE, mother.getParty(),
                MOTHER_COVERSHEET_BINARY, ORDER_NOTIFICATION_BINARY)
        ));
    }

    private void checkCaseDataHasReferenceToSentLetters(UUID firstLetterId,
                                                        UUID secondLetterId,
                                                        Element<Respondent> firstParent,
                                                        Element<Respondent> secondParent) {
        checkUntilSecs(() -> verify(concurrencyHelper, times(2))
            .submitEvent(any(), eq(TEST_CASE_ID), caseDataDelta.capture()), 2);
        List<Element<SentDocuments>> documentsSent = mapper.convertValue(
            caseDataDelta.getValue().get("documentsSentToParties"), new TypeReference<>() {
            }
        );
        assertThat(documentsSent.get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(documentSent(
                firstParent.getValue().getParty(),
                FATHER_COVERSHEET_DOCUMENT,
                ORDER_NOTIFICATION_DOCUMENT,
                firstLetterId,
                now()
            ));
        assertThat(documentsSent.get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(documentSent(
                secondParent.getValue().getParty(),
                MOTHER_COVERSHEET_DOCUMENT, ORDER_NOTIFICATION_DOCUMENT, secondLetterId, now()
            ));
    }

    private Map<String, Object> getExpectedEmailParameters(byte[] fileBytes) {
        final String encodedOrderDocument = new String(Base64.encodeBase64(fileBytes), ISO_8859_1);

        return Map.of(
            "callout", "^Theodore Bailey, " + TEST_FAMILY_MAN_NUMBER,
            "courtName", DEFAULT_LA_COURT,
            "documentLink", Map.of("file", encodedOrderDocument, "filename", JSONObject.NULL,
                "confirm_email_before_download", JSONObject.NULL, "retention_period", JSONObject.NULL),
            "caseUrl", "http://fake-url/cases/case-details/" + TEST_CASE_ID + "#Orders",
            "childLastName", "Bailey"
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(notificationClient, concurrencyHelper, sendLetterApi);
    }

}
