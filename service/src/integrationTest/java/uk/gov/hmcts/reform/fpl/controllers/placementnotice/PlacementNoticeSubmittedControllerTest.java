package uk.gov.hmcts.reform.fpl.controllers.placementnotice;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.controllers.PlacementNoticeController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementNotifyData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.LocalAuthorityRecipientsService;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisCoverDocumentsService;
import uk.gov.hmcts.reform.fpl.service.email.content.PlacementContentProvider;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_UPDATE_EVENT;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_EMAIL;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_NOTICE_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocmosisDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentBinaries;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(PlacementNoticeController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementNoticeSubmittedControllerTest extends AbstractPlacementNoticeControllerTest {

    private static final Long CASE_ID = 12345L;
    private static final String EVENT_TOKEN = randomAlphanumeric(10);
    private static final Document COVERSHEET_DOCUMENT = testDocument();
    private static final Document NOTICE_DOCUMENT = testDocument();
    private static final byte[] COVERSHEET_BINARIES = testDocumentBinaries();
    private static final byte[] NOTICE_BINARIES = testDocumentBinaries();
    private static final PlacementNotifyData PLACEMENT_NOTIFY_DATA = PlacementNotifyData.builder().build();

    @Captor
    private ArgumentCaptor<List<DocumentReference>> documents;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocmosisCoverDocumentsService docmosisCoverDocumentsService;

    @MockBean
    private SendDocumentService sendDocumentService;

    @MockBean
    private PlacementContentProvider placementContentProvider;

    @MockBean
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @MockBean
    private LocalAuthorityRecipientsService localAuthorityRecipientsService;

    @BeforeEach
    void init() {

        givenSystemUser();
        givenFplService();

        given(coreCaseDataApi.startEventForCaseWorker(USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            SYS_USER_ID,
            JURISDICTION,
            CASE_TYPE, CASE_ID.toString(),
            CASE_UPDATE_EVENT))
            .willReturn(StartEventResponse.builder().eventId(CASE_UPDATE_EVENT).token(EVENT_TOKEN).build());
    }

    @Test
    void shouldNotifyAllParties() {

        final Element<Placement> placement = element(Placement.builder()
            .childId(child1.getId())
            .childName(child1.getValue().getParty().getFullName())
            .placementRespondentsToNotify(newArrayList(father))
            .placementNotice(testDocumentReference())
            .build());

        final PlacementEventData placementEventData = PlacementEventData.builder()
            .placement(placement.getValue())
            .placements(newArrayList(placement))
            .placementNoticeVenue("96")
            .placementNoticeDateTime(LocalDateTime.now())
            .placementNoticeDuration("1")
            .build();

        final CaseData caseData = CaseData.builder()
            .children1(List.of(child1, child2))
            .id(CASE_ID)
            .caseLocalAuthority("SA")
            .court(Court.builder().name("Test Court").build())
            .respondents1(List.of(mother, father))
            .placementEventData(placementEventData)
            .build();

        when(docmosisCoverDocumentsService.createCoverDocuments(any(), any(), any(), any()))
            .thenReturn(testDocmosisDocument(COVERSHEET_BINARIES));
        when(uploadDocumentService.uploadPDF(eq(COVERSHEET_BINARIES), any()))
            .thenReturn(COVERSHEET_DOCUMENT);
        when(uploadDocumentService.uploadPDF(eq(NOTICE_BINARIES), any()))
            .thenReturn(NOTICE_DOCUMENT);
        when(documentDownloadService.downloadDocument(any()))
            .thenReturn(NOTICE_BINARIES);
        when(placementContentProvider.getNoticeChangedCafcassData(any(), any()))
            .thenReturn(PLACEMENT_NOTIFY_DATA);
        when(placementContentProvider.getNoticeChangedData(any(), any()))
            .thenReturn(PLACEMENT_NOTIFY_DATA);
        when(cafcassLookupConfiguration.getCafcassWelsh(any())).thenReturn(
            Optional.of(new CafcassLookupConfiguration.Cafcass("test", DEFAULT_CAFCASS_EMAIL)));
        when(localAuthorityRecipientsService.getRecipients(any()))
            .thenReturn(newHashSet(LOCAL_AUTHORITY_1_COURT_EMAIL));

        postSubmittedEvent(caseData);

        checkUntil(() -> {

            verify(notificationClient).sendEmail(
                PLACEMENT_NOTICE_UPLOADED_TEMPLATE,
                LOCAL_AUTHORITY_1_COURT_EMAIL,
                mapper.convertValue(PLACEMENT_NOTIFY_DATA, new TypeReference<>() {}),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                PLACEMENT_NOTICE_UPLOADED_TEMPLATE,
                father.getValue().getSolicitor().getEmail(),
                mapper.convertValue(PLACEMENT_NOTIFY_DATA, new TypeReference<>() {}),
                notificationReference(CASE_ID));

            verify(notificationClient).sendEmail(
                PLACEMENT_NOTICE_UPLOADED_CAFCASS_TEMPLATE,
                DEFAULT_CAFCASS_EMAIL,
                mapper.convertValue(PLACEMENT_NOTIFY_DATA, new TypeReference<>() {}),
                notificationReference(CASE_ID));

            verifyNoMoreInteractions(notificationClient);
        });
    }

    @Test
    void shouldSendDocumentsToRespondentParty() {
        var application = testDocumentReference();
        var placementNotice = testDocumentReference();
        var birthCertificate = PlacementSupportingDocument.builder()
                .type(BIRTH_ADOPTION_CERTIFICATE)
                .document(testDocumentReference())
                .build();
        var father = element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("John")
                .lastName("Doe")
                .relationshipToChild("father")
                .build())
            .build());

        final Element<Placement> placement = element(Placement.builder()
                .childId(child1.getId())
                .childName(child1.getValue().getParty().getFullName())
                .placementRespondentsToNotify(newArrayList(father))
                .placementNotice(placementNotice)
                .application(application)
                .supportingDocuments(
                        List.of(Element.newElement(
                                birthCertificate)))
                .build());

        final PlacementEventData placementEventData = PlacementEventData.builder()
                .placement(placement.getValue())
                .placements(newArrayList(placement))
                .placementNoticeVenue("96")
                .placementNoticeDateTime(LocalDateTime.now())
                .placementNoticeDuration("1")
                .build();

        final CaseData caseData = CaseData.builder()
                .children1(List.of(child1, child2))
                .id(CASE_ID)
                .caseLocalAuthority("SA")
                .court(Court.builder().name("Test Court").build())
                .respondents1(List.of(father))
                .placementEventData(placementEventData)
                .build();

        postSubmittedEvent(caseData);

        checkUntil(() -> {
            verify(sendDocumentService).sendDocuments(isA(CaseData.class),
                    documents.capture(), eq(List.of(father.getValue().getParty())));

            assertThat(documents.getValue().size()).isEqualTo(3);
            assertThat(documents.getValue().contains(application)).isTrue();
            assertThat(documents.getValue().contains(birthCertificate.getDocument())).isTrue();
            assertThat(documents.getValue().contains(placementNotice)).isTrue();

            verifyNoMoreInteractions(sendDocumentService);
        });
    }
}
