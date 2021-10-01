package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ITEM_TRANSLATED_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import static uk.gov.hmcts.reform.fpl.testingsupport.IntegrationTestConstants.COVERSHEET_PDF;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.documentSent;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.printRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadTranslationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsSubmittedControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 1614860986487554L;
    private static final String FAMILY_MAN_CASE_NUMBER = "FMN1";
    private static final UUID LETTER_1_ID = randomUUID();
    private static final UUID LETTER_2_ID = randomUUID();
    private static final UUID LETTER_3_ID = randomUUID();
    private static final UUID LETTER_4_ID = randomUUID();
    private static final Document ORDER_DOCUMENT = testDocument();
    private static final Document ORDER_DOCUMENT_ORIGINAL = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE = testDocument();
    private static final Document COVERSHEET_REPRESENTATIVE_ENGLISH = testDocument();
    private static final Document COVERSHEET_RESPONDENT = testDocument();
    private static final Document COVERSHEET_RESPONDENT_ENGLISH = testDocument();
    private static final byte[] ORDER_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH = testDocumentBinaries();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY = testDocumentBinaries();
    private static final byte[] COVERSHEET_RESPONDENT_BINARY_ENGLISH = testDocumentBinaries();
    private static final DocumentReference TRANSLATED_ORDER = testDocumentReference();
    private static final DocumentReference ORIGINAL_ORDER = testDocumentReference();
    private static final String ORDER_TYPE = "Care order (C32A)";
    private static final Map<String, Object> NOTIFICATION_PARAMETERS = getExpectedParametersMap(ORDER_TYPE);
    private static final Element<Representative> REPRESENTATIVE_POST = element(Representative.builder()
        .fullName("First Representative")
        .servingPreferences(POST)
        .address(testAddress())
        .build());
    private static final Element<Representative> REPRESENTATIVE_EMAIL = element(Representative.builder()
        .fullName("Third Representative")
        .servingPreferences(EMAIL)
        .email("third@representatives.com")
        .build());
    private static final Element<Representative> REPRESENTATIVE_DIGITAL = element(Representative.builder()
        .fullName("Second Representative")
        .servingPreferences(DIGITAL_SERVICE)
        .email("second@representatives.com")
        .build());
    private static final Respondent RESPONDENT_NOT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Alex")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .build();
    private static final Respondent RESPONDENT_WITHOUT_ADDRESS = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Emma")
            .lastName("Jones")
            .build())
        .build();
    private static final Respondent RESPONDENT_REPRESENTED = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("George")
            .lastName("Jones")
            .address(testAddress())
            .build())
        .representedBy(wrapElements(REPRESENTATIVE_POST.getId(), REPRESENTATIVE_DIGITAL.getId()))
        .build();
    private static final long ASYNC_METHOD_CALL_TIMEOUT = 10000;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataDelta;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> printRequest;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService documentService;

    UploadTranslationsSubmittedControllerTest() {
        super("upload-translations");
    }

    @BeforeEach
    void mocks() {
        givenFplService();
        when(documentDownloadService.downloadDocument(anyString()))
            .thenReturn(ORDER_BINARY);
        when(uploadDocumentService.uploadPDF(ORDER_BINARY, TRANSLATED_ORDER.getFilename()))
            .thenReturn(ORDER_DOCUMENT);
        when(uploadDocumentService.uploadPDF(ORDER_BINARY, ORIGINAL_ORDER.getFilename()))
            .thenReturn(ORDER_DOCUMENT_ORIGINAL);

        when(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue()), eq(WELSH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_REPRESENTATIVE);

        when(documentService.createCoverDocuments(any(), any(), eq(REPRESENTATIVE_POST.getValue()), eq(ENGLISH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_REPRESENTATIVE_ENGLISH);

        when(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty()), eq(WELSH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_RESPONDENT);

        when(documentService.createCoverDocuments(any(), any(), eq(RESPONDENT_NOT_REPRESENTED.getParty()), eq(ENGLISH)))
            .thenReturn(DocmosisDocument.builder().bytes(COVERSHEET_RESPONDENT_BINARY_ENGLISH).build());
        when(uploadDocumentService.uploadPDF(COVERSHEET_RESPONDENT_BINARY_ENGLISH, COVERSHEET_PDF))
            .thenReturn(COVERSHEET_RESPONDENT_ENGLISH);

        when(sendLetterApi.sendLetter(any(), any(LetterWithPdfsRequest.class)))
            .thenReturn(
                new SendLetterResponse(LETTER_1_ID),
                new SendLetterResponse(LETTER_2_ID),
                new SendLetterResponse(LETTER_3_ID),
                new SendLetterResponse(LETTER_4_ID)
            );
    }

    @Test
    void shouldSendTranslatedNotificationToLocalAuthorityWhenTranslatedOrder() {
        CaseData caseData = caseData();
        CallbackRequest request = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .build();

        postSubmittedEvent(request);

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                ITEM_TRANSLATED_NOTIFICATION_TEMPLATE, LOCAL_AUTHORITY_1_INBOX,
                NOTIFICATION_PARAMETERS, notificationReference(CASE_ID)
            )
        );
    }

    @Test
    void shouldSendOrdersByPostWhenOrderTranslated() {

        postSubmittedEvent(caseData());

        checkUntil(() -> {
            verify(sendLetterApi, times(4)).sendLetter(eq(SERVICE_AUTH_TOKEN),
                printRequest.capture());
            verify(coreCaseDataService).updateCase(eq(CASE_ID), caseDataDelta.capture());
        });

        assertThat(printRequest.getAllValues()).usingRecursiveComparison()
            .isEqualTo(List.of(
                printRequest(CASE_ID, ORIGINAL_ORDER, COVERSHEET_REPRESENTATIVE_BINARY_ENGLISH, ORDER_BINARY),
                printRequest(CASE_ID, ORIGINAL_ORDER, COVERSHEET_RESPONDENT_BINARY_ENGLISH, ORDER_BINARY),
                printRequest(CASE_ID, TRANSLATED_ORDER, COVERSHEET_REPRESENTATIVE_BINARY, ORDER_BINARY),
                printRequest(CASE_ID, TRANSLATED_ORDER, COVERSHEET_RESPONDENT_BINARY, ORDER_BINARY)
            ));

        List<Element<SentDocuments>> documentsSent = mapper.convertValue(
            caseDataDelta.getValue().get("documentsSentToParties"), new TypeReference<>() {
            }
        );

        assertThat(documentsSent.get(0).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(
                documentSent(
                    REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE_ENGLISH, ORDER_DOCUMENT_ORIGINAL, LETTER_1_ID, now()
                ),
                documentSent(
                    REPRESENTATIVE_POST.getValue(),
                    COVERSHEET_REPRESENTATIVE, ORDER_DOCUMENT, LETTER_3_ID, now()
                )
            );

        assertThat(documentsSent.get(1).getValue().getDocumentsSentToParty())
            .extracting(Element::getValue)
            .containsExactly(
                documentSent(
                    RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT_ENGLISH, ORDER_DOCUMENT_ORIGINAL, LETTER_2_ID, now()
                ),
                documentSent(
                    RESPONDENT_NOT_REPRESENTED.getParty(),
                    COVERSHEET_RESPONDENT, ORDER_DOCUMENT, LETTER_4_ID, now()
                )
            );
    }

    @Test
    void shouldNotifyRepresentativesServedDigitallyWhenOrderTranslated() {

        postSubmittedEvent(caseData());

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                ITEM_TRANSLATED_NOTIFICATION_TEMPLATE,
                REPRESENTATIVE_DIGITAL.getValue().getEmail(),
                NOTIFICATION_PARAMETERS,
                notificationReference(CASE_ID)
            )
        );
    }

    @Test
    void shouldNotifyRepresentativesServedByEmailWhenOrderTranslated() {

        postSubmittedEvent(caseData());

        checkUntil(() ->
            verify(notificationClient).sendEmail(
                ITEM_TRANSLATED_NOTIFICATION_TEMPLATE,
                REPRESENTATIVE_EMAIL.getValue().getEmail(),
                NOTIFICATION_PARAMETERS,
                notificationReference(CASE_ID)
            )
        );
    }

    private CaseData caseData() {
        return CaseData.builder()
            .id(CASE_ID)
            .children1(List.of(element(Child.builder()
                .party(ChildParty.builder()
                    .lastName("ChildLast")
                    .dateOfBirth(LocalDate.of(2012, 1, 2))
                    .build())
                .build())))
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .orderCollection(wrapElements(GeneratedOrder.builder()
                .orderType("C32A_CARE_ORDER")
                .type(ORDER_TYPE)
                .translationUploadDateTime(now().plusSeconds(2))
                .translatedDocument(TRANSLATED_ORDER)
                .document(ORIGINAL_ORDER)
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()))
            .respondents1(wrapElements(RESPONDENT_NOT_REPRESENTED, RESPONDENT_WITHOUT_ADDRESS, RESPONDENT_REPRESENTED))
            .representatives(List.of(REPRESENTATIVE_POST, REPRESENTATIVE_DIGITAL, REPRESENTATIVE_EMAIL))
            .build();
    }

    private static Map<String, Object> getExpectedParametersMap(String orderType) {
        return Map.of(
            "childLastName", "ChildLast",
            "docType", orderType,
            "callout", "^Jones, FMN1",
            "courtName", "Family Court",
            "caseUrl", "http://fake-url/cases/case-details/1614860986487554"
        );
    }
}
