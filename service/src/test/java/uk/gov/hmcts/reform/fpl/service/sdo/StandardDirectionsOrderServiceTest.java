package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
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
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(MockitoExtension.class)
class StandardDirectionsOrderServiceTest {
    private static final String USER_NAME = "Adam";
    private static final DocumentReference SEALED_DOC = DocumentReference.builder().filename("sealed.pdf").build();
    private static final DocumentReference WORD_DOC = DocumentReference.builder().filename("word.docx").build();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();
    private static final String JUDGE_NAME = "Davidson";
    private static final JudgeOrMagistrateTitle JUDGE_TITLE = HIS_HONOUR_JUDGE;

    @Mock
    private DocumentConversionService conversionService;

    @Mock
    private DocumentSealingService sealingService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private IdamClient idamClient;

    @Mock
    private RequestData requestData;

    private StandardDirectionsOrderService service;
    private JudgeAndLegalAdvisor judgeAndLegalAdvisor;

    @BeforeEach
    void setUp() {
        service = new StandardDirectionsOrderService(sealingService, featureToggleService, TIME, idamClient,
            requestData);

        judgeAndLegalAdvisor = buildJudgeAndLegalAdvisor();
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
            .preparedSDO(WORD_DOC)
            .build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, null);

        StandardDirectionOrder expectedOrder = StandardDirectionOrder.builder().orderDoc(WORD_DOC).build();

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
    void shouldSetJudgeAndLegalAdvisorOnSDOWhenWhenSendNoticeOfProceedings() {

        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder()
            .orderDoc(SEALED_DOC)
            .build();

        CaseData caseData = CaseData.builder()
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .preparedSDO(null)
            .replacementSDO(null)
            .build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, previousSDO);

        assertThat(order.getJudgeAndLegalAdvisor().getAllocatedJudgeLabel()).isNull();
        assertThat(order.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
    }

    @Test
    void shouldNotSetJudgeAndLegalAdvisorOnSDOWhenWhenJudgeAndLegalAdvisorIsNotPopulated() {

        StandardDirectionOrder previousSDO = StandardDirectionOrder.builder()
            .orderDoc(SEALED_DOC)
            .build();

        CaseData caseData = CaseData.builder()
            .preparedSDO(null)
            .replacementSDO(null)
            .build();

        StandardDirectionOrder order = service.buildTemporarySDO(caseData, previousSDO);

        assertThat(order.getJudgeAndLegalAdvisor()).isNull();
    }

    @Test
    void shouldNotSealDocumentWhenSDOIsDraft() {
        mockIdamAndRequestData();
        StandardDirectionOrder order = buildStandardDirectionOrder(WORD_DOC, DRAFT, judgeAndLegalAdvisor);

        StandardDirectionOrder builtOrder = service.buildOrderFromUpload(order, SealType.ENGLISH);

        StandardDirectionOrder expectedOrder = buildStandardDirectionOrder(
            WORD_DOC, null, DRAFT, TIME.now().toLocalDate(), USER_NAME, judgeAndLegalAdvisor
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);
        verifyNoInteractions(conversionService, conversionService);
    }

    @Test
    void shouldSealDocumentWhenSDOIsToBeSealed() {
        mockSealingService();
        mockIdamAndRequestData();
        StandardDirectionOrder order = buildStandardDirectionOrder(WORD_DOC, SEALED, judgeAndLegalAdvisor);

        StandardDirectionOrder builtOrder = service.buildOrderFromUpload(order, SealType.ENGLISH);

        StandardDirectionOrder expectedOrder = buildStandardDirectionOrder(
            SEALED_DOC, WORD_DOC, SEALED, TIME.now().toLocalDate(), USER_NAME, judgeAndLegalAdvisor
        );

        assertThat(builtOrder).isEqualTo(expectedOrder);

        verify(sealingService).sealDocument(WORD_DOC, SealType.ENGLISH);
    }

    @Test
    void shouldConvertWordDocumentAndSealWhenSDOIsSetToSeal() {
        given(sealingService.sealDocument(WORD_DOC, SealType.ENGLISH)).willReturn(SEALED_DOC);
        mockIdamAndRequestData();

        StandardDirectionOrder order = buildStandardDirectionOrder(WORD_DOC, SEALED, judgeAndLegalAdvisor);
        StandardDirectionOrder standardDirectionOrder = service.buildOrderFromUpload(order, SealType.ENGLISH);

        assertThat(standardDirectionOrder.getOrderDoc()).isEqualTo(SEALED_DOC);
    }

    @Test
    void shouldPersistJudgeAndLegalAdvisorWhenSendingNoticeOfProceedings() {
        given(sealingService.sealDocument(WORD_DOC, SealType.ENGLISH)).willReturn(SEALED_DOC);
        mockIdamAndRequestData();

        StandardDirectionOrder order = buildStandardDirectionOrder(WORD_DOC, SEALED, judgeAndLegalAdvisor);
        StandardDirectionOrder standardDirectionOrder = service.buildOrderFromUpload(order, SealType.ENGLISH);

        assertThat(standardDirectionOrder.getJudgeAndLegalAdvisor()).isEqualTo(judgeAndLegalAdvisor);
    }

    @Test
    void shouldReturnJudgeAndLegalAdvisorFromSDOWhenSDOContainsJudgeAndLegalAdvisor() {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JUDGE_TITLE)
            .judgeLastName(JUDGE_NAME)
            .build();

        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .build())
            .build();

        JudgeAndLegalAdvisor actualJudgeAndLegalAdvisor = service.getJudgeAndLegalAdvisorFromSDO(caseData);

        assertThat(actualJudgeAndLegalAdvisor).isEqualTo(judgeAndLegalAdvisor);
    }

    @Test
    void shouldPrepareJudgeAndLegalAdvisorLabelFromAllocatedJudgeWhenCMODoesNotExist() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeTitle(JUDGE_TITLE)
                .judgeLastName(JUDGE_NAME)
                .build())
            .build();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = service.getJudgeAndLegalAdvisorFromSDO(caseData);

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo(String.format("Case assigned to: %s %s", JUDGE_TITLE.getLabel(), JUDGE_NAME));
    }

    @Test
    void shouldUpdateAllocatedJudgePropertiesOnSDOJudgeWhenAllocatedJudgeExists() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeLastName("Watson")
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .build())
                .build())
            .allocatedJudge(Judge.builder()
                .judgeTitle(JUDGE_TITLE)
                .judgeLastName(JUDGE_NAME)
                .build())
            .build();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = service.getJudgeAndLegalAdvisorFromSDO(caseData);

        assertThat(judgeAndLegalAdvisor.getAllocatedJudgeLabel())
            .isEqualTo(String.format("Case assigned to: %s %s", JUDGE_TITLE.getLabel(), JUDGE_NAME));

        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo(NO.getValue());
    }

    @Test
    void shouldSetIsUsingAllocatedJudgePropertyToYesWhenJudgeOnBothSDOAndAllocatedJudgeMatch() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeLastName(JUDGE_NAME)
                    .judgeTitle(JUDGE_TITLE)
                    .build())
                .build())
            .allocatedJudge(Judge.builder()
                .judgeTitle(JUDGE_TITLE)
                .judgeLastName(JUDGE_NAME)
                .build())
            .build();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = service.getJudgeAndLegalAdvisorFromSDO(caseData);

        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo(YES.getValue());
    }

    @Test
    void shouldReturnEmptyJudgeIfJudgeOnSDOAndAllocatedJudgeDoNotExist() {
        CaseData caseData = CaseData.builder()
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .build();

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = service.getJudgeAndLegalAdvisorFromSDO(caseData);

        assertThat(judgeAndLegalAdvisor).isEqualTo(JudgeAndLegalAdvisor.builder().build());
    }

    private StandardDirectionOrder buildStandardDirectionOrder(DocumentReference document, OrderStatus status,
                                                               JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return buildStandardDirectionOrder(document, null, status, null, null, judgeAndLegalAdvisor);
    }

    private StandardDirectionOrder buildStandardDirectionOrder(DocumentReference document,
                                                               DocumentReference lastUploadedDocument,
                                                               OrderStatus status, LocalDate dateOfUpload,
                                                               String uploader,
                                                               JudgeAndLegalAdvisor judgeAndLegalAdvisor) {
        return StandardDirectionOrder.builder()
            .orderDoc(document)
            .lastUploadedOrder(lastUploadedDocument)
            .orderStatus(status)
            .dateOfUpload(dateOfUpload)
            .uploader(uploader)
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .build();
    }

    private void mockIdamAndRequestData() {
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(UserInfo.builder().name(USER_NAME).build());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    private void mockSealingService() {
        given(sealingService.sealDocument(WORD_DOC, SealType.ENGLISH)).willReturn(SEALED_DOC);
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(JUDGE_TITLE)
            .allocatedJudgeLabel("Some label")
            .judgeLastName(JUDGE_NAME)
            .build();
    }
}
