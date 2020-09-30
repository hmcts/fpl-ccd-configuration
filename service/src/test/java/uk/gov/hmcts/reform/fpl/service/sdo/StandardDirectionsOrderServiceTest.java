package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.USER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@ExtendWith(MockitoExtension.class)
class StandardDirectionsOrderServiceTest {
    private static final String USER_NAME = "Adam";
    private static final DocumentReference SEALED_DOC = DocumentReference.builder().filename("sealed.pdf").build();
    private static final DocumentReference WORD_DOC = DocumentReference.builder().filename("word.docx").build();
    private static final DocumentReference PDF_DOC = DocumentReference.builder().filename("doc.pdf").build();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();

    @Mock
    private DocumentConversionService conversionService;
    @Mock
    private DocumentSealingService sealingService;
    @Mock
    private IdamClient idamClient;
    @Mock
    private RequestData requestData;
    private StandardDirectionsOrderService service;

    @BeforeEach
    void setUp() {
        service = new StandardDirectionsOrderService(sealingService, TIME, idamClient, requestData);
    }

    @Test
    void shouldPullDateFromStandardDirectionOrderWhenPresent() {
        StandardDirectionOrder order = StandardDirectionOrder.builder()
            .dateOfIssue("22 March 2020")
            .build();

        LocalDate dateOfIssue = service.generateDateOfIssue(order);

        assertThat(dateOfIssue).isEqualTo(LocalDate.of(2020, 3, 22));
    }

    @Test
    void shouldSetDateOfIssueToCurrentDateWhenStandardDirectionOrderDoesNotHaveDateOfIssue() {
        LocalDate dateOfIssue = service.generateDateOfIssue(null);

        assertThat(dateOfIssue).isEqualTo(TIME.now().toLocalDate());
    }

    @Test
    void shouldUsePreparedSDOForTemporaryStandardDirectionOrder() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(PDF_DOC)
            .build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, null);

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(PDF_DOC).build();

        assertThat(order).isEqualTo(expectedOrder);
    }

    @Test
    void shouldUseReplacementSDOForTemporaryStandardDirectionOrder() {
        CaseData caseData = CaseData.builder()
            .preparedSDO(null)
            .replacementSDO(WORD_DOC)
            .build();

        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder().build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, previousSDO);

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(WORD_DOC).build();

        assertThat(order).isEqualTo(expectedOrder);
    }

    @Test
    void shouldUseCurrentSDODocumentForTemporaryStandardDirectionOrder() {
        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder()
            .orderDoc(SEALED_DOC)
            .build();

        CaseData caseData = CaseData.builder()
            .preparedSDO(null)
            .replacementSDO(null)
            .build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, previousSDO);

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(SEALED_DOC).build();

        assertThat(order).isEqualTo(expectedOrder);
    }

    @Test
    void shouldNotSealDocumentWhenSDOIsDraft() throws Exception {
        mockIdamAndRequestData();

        StandardDirectionOrder order = buildStandardDirectionOrder(PDF_DOC, DRAFT);

        StandardDirectionOrder builtOrder = service.buildOrderFromUpload(order);

        StandardDirectionOrder expectedOrder = buildStandardDirectionOrder(
            PDF_DOC, DRAFT, TIME.now().toLocalDate(), USER_NAME
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);
        verifyNoInteractions(conversionService, conversionService);
    }

    @Test
    void shouldSealDocumentWhenSDOIsToBeSealed() throws Exception {
        mockSealingService();
        mockIdamAndRequestData();

        StandardDirectionOrder order = buildStandardDirectionOrder(PDF_DOC, SEALED);

        StandardDirectionOrder builtOrder = service.buildOrderFromUpload(order);

        StandardDirectionOrder expectedOrder = buildStandardDirectionOrder(
            SEALED_DOC, SEALED, TIME.now().toLocalDate(), USER_NAME
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);

        verify(sealingService).sealDocument(PDF_DOC);
    }

    @Test
    void shouldConvertWordDocumentAndSealWhenSDOIsSetToSeal() throws Exception {
        StandardDirectionOrder order = buildStandardDirectionOrder(WORD_DOC, SEALED);

        given(sealingService.sealDocument(WORD_DOC)).willReturn(SEALED_DOC);
        mockIdamAndRequestData();

        StandardDirectionOrder standardDirectionOrder = service.buildOrderFromUpload(order);

        assertThat(standardDirectionOrder.orderDoc).isEqualTo(SEALED_DOC);
    }

    private StandardDirectionOrder buildStandardDirectionOrder(DocumentReference document, OrderStatus status) {
        return buildStandardDirectionOrder(document, status, null, null);
    }

    private StandardDirectionOrder buildStandardDirectionOrder(DocumentReference document, OrderStatus status,
                                                               LocalDate dateOfUpload, String uploader) {
        return StandardDirectionOrder.builder()
            .orderDoc(document)
            .orderStatus(status)
            .dateOfUpload(dateOfUpload)
            .uploader(uploader)
            .build();
    }

    private void mockIdamAndRequestData() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name(USER_NAME).build());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    private void mockSealingService() throws Exception {
        given(sealingService.sealDocument(PDF_DOC)).willReturn(SEALED_DOC);
    }
}
