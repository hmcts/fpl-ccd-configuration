package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fnp.client.FeesRegisterApi;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.workallocation.WorkAllocationTaskService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_ADMIN_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CTSC_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.ASYNC_MAX_TIMEOUT;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feeResponse;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlacementSubmittedControllerTest extends AbstractPlacementControllerTest {

    private static final Long CASE_ID = 12345L;
    private static final UUID LETTER_ID = randomUUID();
    private static final BigDecimal FEE = BigDecimal.valueOf(455.5);
    private static final String PBA_NUMBER = "PBA1234567";
    private static final String EVENT_TOKEN = randomAlphanumeric(10);
    private static final Document COVERSHEET_DOCUMENT = testDocument();
    private static final Document FIRST_PARENT_NOTICE_DOCUMENT = testDocument();
    private static final byte[] COVERSHEET_BINARIES = testDocumentBinaries();
    private static final byte[] CAFCASS_NOTICE_BINARIES = testDocumentBinaries();
    private static final byte[] FIRST_PARENT_NOTICE_BINARIES = testDocumentBinaries();
    public static final String INTERNAL_CHANGE_PLACEMENT = "internal-change-placement";

    @MockBean
    private PaymentApi paymentApi;

    @MockBean
    private FeesRegisterApi feesRegisterApi;

    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private SendLetterApi sendLetterApi;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @Captor
    private ArgumentCaptor<LetterWithPdfsRequest> sendLetterRequestCaptor;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private WorkAllocationTaskService workAllocationTaskService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void init() {

        givenSystemUser();
        givenFplService();

        given(feesRegisterApi.findFee("default", "miscellaneous", "family", "family court", "Placement", "adoption"))
            .willReturn(feeResponse(FEE.doubleValue()));
        given(featureToggleService.isWATaskEmailsEnabled()).willReturn(true);
    }

    @Test
    void shouldMakePaymentAndSendNotificationToPartiesAboutSubmittedPlacementApplication() {

        final Placement placement = Placement.builder()
            .childId(child1.getId())
            .application(testDocumentReference())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placementPayment(PBAPayment.builder()
                .pbaNumber(PBA_NUMBER)
                .build())
            .placements(wrapElements(placement))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .children1(List.of(child1, child2))
            .respondents1(List.of(mother, father))
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .placementEventData(placementEventData)
            .build();

        when(paymentApi.createCreditAccountPayment(any(), any(), any()))
            .thenReturn(CreditAccountPaymentRequest.builder().build());
        when(docmosisCoverDocumentsService.createCoverDocuments(any(), any(), any(), any()))
            .thenReturn(testDocmosisDocument(COVERSHEET_BINARIES));
        when(uploadDocumentService.uploadPDF(eq(COVERSHEET_BINARIES), any()))
            .thenReturn(COVERSHEET_DOCUMENT);
        when(uploadDocumentService.uploadPDF(eq(FIRST_PARENT_NOTICE_BINARIES), any()))
            .thenReturn(FIRST_PARENT_NOTICE_DOCUMENT);
        when(sendLetterApi.sendLetter(anyString(), any(LetterWithPdfsRequest.class)))
            .thenReturn(new SendLetterResponse(LETTER_ID));
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        final CreditAccountPaymentRequest expectedPaymentRequest = expectedCreditAccountPaymentRequest();
        final Map<String, Object> expectedCaseUpdateRequest = expectedCaseDataContent();

        verify(paymentApi).createCreditAccountPayment(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);

        verify(concurrencyHelper, timeout(ASYNC_MAX_TIMEOUT)).submitEvent(
            any(),
            eq(CASE_ID),
            eq(expectedCaseUpdateRequest));

        checkUntil(() -> {
            verify(notificationClient).sendEmail(
                PLACEMENT_APPLICATION_UPLOADED_COURT_TEMPLATE,
                DEFAULT_ADMIN_EMAIL,
                Map.of(
                    "caseUrl", getPlacementTabUrl(CASE_ID),
                    "localAuthority", LOCAL_AUTHORITY_1_NAME),
                notificationReference(CASE_ID));

            verifyNoMoreInteractions(notificationClient);
        });
    }

    @Test
    void shouldNotifyCourtAndApplicantAboutPlacementPaymentFailure() throws NotificationClientException {

        final Placement placement = Placement.builder()
            .childId(child1.getId())
            .build();

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placementPaymentRequired(YES)
            .placementPayment(PBAPayment.builder()
                .pbaNumber(PBA_NUMBER)
                .build())
            .placements(wrapElements(placement))
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorities(wrapElementsWithUUIDs(LocalAuthority.builder()
                .id(LOCAL_AUTHORITY_1_CODE)
                .designated(YES.getValue())
                .email(LOCAL_AUTHORITY_1_INBOX)
                .build()))
            .children1(List.of(child1, child2))
            .placementEventData(placementEventData)
            .build();

        when(paymentApi.createCreditAccountPayment(any(), any(), any())).thenThrow(feignException(403));

        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        final CreditAccountPaymentRequest expectedPaymentRequest = expectedCreditAccountPaymentRequest();
        final Map<String, Object> expectedCaseUpdateRequest = expectedCaseDataContent();

        verify(paymentApi).createCreditAccountPayment(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest);

        verify(concurrencyHelper, timeout(ASYNC_MAX_TIMEOUT)).submitEvent(
            any(),
            eq(CASE_ID),
            eq(expectedCaseUpdateRequest));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_CTSC,
            DEFAULT_CTSC_EMAIL,
            Map.of(
                "caseUrl", getPlacementTabUrl(CASE_ID),
                "applicationType", "A50 - Application for a Placement Order",
                "applicant", LOCAL_AUTHORITY_1_NAME),
            notificationReference(CASE_ID));

        verify(notificationClient).sendEmail(
            INTERLOCUTORY_PBA_PAYMENT_FAILED_TEMPLATE_FOR_APPLICANT,
            LOCAL_AUTHORITY_1_INBOX,
            Map.of(
                "caseUrl", caseUrl(caseData.getId(), "Placement"),
                "applicationType", "A50 - Application for a Placement Order"),
            notificationReference(CASE_ID));
    }

    @Test
    void shouldNotMakePaymentForPlacementIfNotRequired() {

        final Placement placement = Placement.builder()
            .childId(child1.getId())
            .build();

        final CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placementPaymentRequired(NO)
                .placement(placement)
                .placements(wrapElements(placement))
                .build())
            .build();
        doAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build()).when(concurrencyHelper).startEvent(any(), any(String.class));

        postSubmittedEvent(caseData);

        verifyNoInteractions(feesRegisterApi, paymentApi, notificationClient);
    }

    @Test
    void shouldNotMakePaymentWhenPlacementIsUpdated() {

        final Placement existingApplicationForChild1 = Placement.builder()
            .childId(child1.getId())
            .application(testDocumentReference())
            .build();

        final Placement existingApplicationForChild2 = Placement.builder()
            .childId(child2.getId())
            .application(testDocumentReference())
            .confidentialDocuments(wrapElements(annexB))
            .build();

        final Placement newApplicationForChild2 = existingApplicationForChild2.toBuilder()
            .confidentialDocuments(wrapElements(annexB, guardiansReport))
            .build();

        final CaseData caseDataBefore = CaseData.builder()
            .id(CASE_ID)
            .caseLocalAuthorityName(LOCAL_AUTHORITY_1_NAME)
            .children1(List.of(child1, child2))
            .placementEventData(PlacementEventData.builder()
                .placements(wrapElements(existingApplicationForChild1, existingApplicationForChild2))
                .build())
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .placementEventData(PlacementEventData.builder()
                .placement(newApplicationForChild2)
                .placements(wrapElements(existingApplicationForChild1, newApplicationForChild2))
                .build())
            .build();
        when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(i.getArgument(1))
            .token("token")
            .build());

        postSubmittedEvent(toCallBackRequest(caseData, caseDataBefore));

        verifyNoInteractions(notificationClient, paymentApi);
    }

    @Nested
    class TestSealing {

        private final Document sealedDocument = testDocument();
        private final DocumentReference sealedApplication = buildFromDocument(sealedDocument);
        private final DocumentReference application = testDocumentReference("application.doc");

        @BeforeEach
        void init() {
            final byte[] applicationContent = testDocumentBinaries();
            final byte[] applicationContentAsPdf = readBytes("documents/document.pdf");
            final byte[] sealedApplicationContent = readBytes("documents/document-sealed.pdf");

            when(documentDownloadService.downloadDocument(application.getBinaryUrl()))
                .thenReturn(applicationContent);

            when(documentConversionService.convertToPdf(applicationContent, application.getFilename()))
                .thenReturn(applicationContentAsPdf);

            when(uploadDocumentService.uploadPDF(sealedApplicationContent, "application.pdf"))
                .thenReturn(sealedDocument);

            given(concurrencyHelper.startEvent(any(), eq(INTERNAL_CHANGE_PLACEMENT)))
                .willReturn(StartEventResponse.builder().eventId(INTERNAL_CHANGE_PLACEMENT).token(EVENT_TOKEN).build());
        }

        @Test
        void shouldSealPlacementApplication() {
            UUID applicationUUID = randomUUID();

            Placement placement = Placement.builder()
                .childId(child1.getId())
                .application(application)
                .build();

            final CaseData caseData = CaseData.builder()
                .id(1L)
                .children1(List.of(child1, child2))
                .placementEventData(PlacementEventData.builder()
                    .placements(List.of(element(applicationUUID, placement)))
                    .placementIdToBeSealed(applicationUUID)
                    .placementPaymentRequired(NO)
                    .build())
                .build();
            when(concurrencyHelper.startEvent(any(), any(String.class))).thenAnswer(i -> StartEventResponse.builder()
                .caseDetails(asCaseDetails(caseData))
                .eventId(i.getArgument(1))
                .token("token")
                .build());

            postSubmittedEvent(caseData);

            Placement sealedPlacement = placement.toBuilder().application(sealedApplication).build();

            final Map<String, Object> expectedCaseChanges = new HashMap<>();
            expectedCaseChanges.put("placements", List.of(element(applicationUUID, sealedPlacement)));

            verify(concurrencyHelper).submitEvent(any(), eq(1L), eq(expectedCaseChanges));
        }
    }

    private CreditAccountPaymentRequest expectedCreditAccountPaymentRequest() {

        return CreditAccountPaymentRequest.builder()
            .accountNumber(PBA_NUMBER)
            .amount(FEE)
            .caseReference("Not provided")
            .currency(GBP)
            .customerReference("Not provided")
            .description(String.format("Payment for case: %s", CASE_ID))
            .service(FPL)
            .siteId("SITE_ID")
            .fees(List.of(FeeDto.builder()
                .calculatedAmount(FEE)
                .build()))
            .ccdCaseNumber(CASE_ID.toString())
            .organisationName(LOCAL_AUTHORITY_1_NAME)
            .build();
    }

    private Map<String, Object> expectedCaseDataContent() {
        final Map<String, Object> expectedCaseChanges = new HashMap<>();

        expectedCaseChanges.put("placementLastPaymentTime", now());
        expectedCaseChanges.put("placementPaymentRequired", null);
        expectedCaseChanges.put("placementPayment", null);

        return expectedCaseChanges;
    }
}
