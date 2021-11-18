package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;
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
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_FIRST;
import static uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument.RecipientType.PARENT_SECOND;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
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
    private CoreCaseDataService coreCaseDataService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataDelta;

    ManageOrdersForPlacementOrderSubmittedControllerTest() {
        super("manage-orders");
    }

    @BeforeEach
    void setUp() {
        givenFplService();
    }

    @Test
    void shouldSendPlacementOrderAndOrderNotificationToExpectedPartiesWhenChildAndParentsAreNotRepresented() {
        Element<Respondent> father = testRespondentWithAddress("Father", "Jones");
        Element<Respondent> mother = testRespondentWithAddress("Mother", "Jones");

        when(documentDownloadService.downloadDocument(ORDER_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_BINARY);
        when(documentDownloadService.downloadDocument(ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getBinaryUrl()))
            .thenReturn(ORDER_NOTIFICATION_BINARY);
        when(uploadDocumentService.uploadPDF(ORDER_NOTIFICATION_BINARY,
            ORDER_NOTIFICATION_DOCUMENT_REFERENCE.getFilename()))
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

        postSubmittedEvent(toCallBackRequest(placementOrderCaseData, CaseData.builder().build()));

        //Order
        checkPlacementOrderWasDeliveredAsExpected();
        //Order notification
        checkOrderNotificationLetterWasMailedToParents();
        checkCaseDataHasReferenceToSentLetters(firstLetterId, secondLetterId, father, mother);
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
        postSubmittedEvent(toCallBackRequest(placementOrderCaseData, CaseData.builder().build()));

        //Order
        checkPlacementOrderWasDeliveredAsExpected();
        //Order notification
        checkEmailWithOrderNotificationWasSentToParentsSolicitors();
        checkEmailWithOrderNotificationWasSentToChildSolicitor();
    }

    private CaseData buildCaseData(Element<Child> child,
                                   Element<Respondent> firstParent,
                                   Element<Respondent> secondParent) {
        return CaseData.builder()
            .id(TEST_CASE_ID)
            .familyManCaseNumber(TEST_FAMILY_MAN_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .respondents1(List.of(ANOTHER_RESPONDENT, firstParent, secondParent))
            .children1(List.of(child))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(
                    Placement.builder().childId(child.getId()).noticeDocuments(wrapElements(
                        PlacementNoticeDocument.builder().type(CAFCASS)
                            .respondentId(ANOTHER_RESPONDENT.getId()).build(),
                        PlacementNoticeDocument.builder().type(PARENT_SECOND)
                            .respondentId(firstParent.getId()).build(),
                        PlacementNoticeDocument.builder().type(PARENT_FIRST)
                            .respondentId(secondParent.getId()).build()
                    )).build()
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
        checkEmailWithOrderWasSent(CAFCASS_EMAIL);
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

    private void checkOrderNotificationLetterWasMailedToParents() {
        checkUntil(() -> verify(sendLetterApi, times(2)).sendLetter(
            eq(SERVICE_AUTH_TOKEN),
            letterCaptor.capture()
        ));
        assertThat(letterCaptor.getAllValues()).usingRecursiveComparison().isEqualTo(List.of(
            printRequest(TEST_CASE_ID, ORDER_NOTIFICATION_DOCUMENT_REFERENCE,
                FATHER_COVERSHEET_BINARY, ORDER_NOTIFICATION_BINARY),
            printRequest(TEST_CASE_ID, ORDER_NOTIFICATION_DOCUMENT_REFERENCE,
                MOTHER_COVERSHEET_BINARY, ORDER_NOTIFICATION_BINARY)
        ));
    }

    private void checkCaseDataHasReferenceToSentLetters(UUID firstLetterId,
                                                        UUID secondLetterId,
                                                        Element<Respondent> firstParent,
                                                        Element<Respondent> secondParent) {
        checkUntil(() -> verify(coreCaseDataService).updateCase(eq(TEST_CASE_ID), caseDataDelta.capture()));
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
            "documentLink", Map.of("file", encodedOrderDocument, "is_csv", false),
            "caseUrl", "http://fake-url/cases/case-details/" + TEST_CASE_ID + "#Orders",
            "childLastName", "Bailey"
        );
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(notificationClient, coreCaseDataService, sendLetterApi);
    }

}
