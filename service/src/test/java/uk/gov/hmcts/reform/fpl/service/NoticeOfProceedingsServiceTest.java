package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.noticeofproceedings.NoticeOfProceedingsLanguageFactory;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.SecureDocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JacksonAutoConfiguration.class, RestTemplate.class, CaseDataExtractionService.class,
    HearingVenueLookUpService.class, FixedTimeConfiguration.class, NoticeOfHearingGenerationService.class,
    NoticeOfProceedingsLanguageFactory.class,
    LookupTestConfig.class, DocmosisConfiguration.class, NoticeOfProceedingsService.class, CaseDetailsHelper.class,
    CourtService.class, HighCourtAdminEmailLookupConfiguration.class})
class NoticeOfProceedingsServiceTest {
    private static final String JUDGE_SURNAME = "Davidson";
    private static final byte[] PDF = {1, 2, 3, 4, 5};

    @Autowired
    private NoticeOfProceedingsService noticeOfProceedingService;

    @Autowired
    private Time time;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    private LocalDateTime now;

    @BeforeEach
    void before() {
        now = time.now();
    }

    @Test
    void shouldSetHearingAndJudgeLabelWhenBothHearingAndJudgeInformationExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(buildAllocatedJudge())
            .hearingDetails(createHearingBookings())
            .build();

        Map<String, Object> listAndLabel = noticeOfProceedingService.initNoticeOfProceeding(caseData);

        assertThat(listAndLabel)
            .extracting("proceedingLabel", "noticeOfProceedings")
            .containsExactly(buildExpectedHearingLabel(), buildExpectedNoticeOfProceedingData());
    }

    @Test
    void shouldSetAllocatedJudgeLabelWhenOnlyJudgeDataExistsOnCaseData() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(buildAllocatedJudge())
            .build();

        Map<String, Object> listAndLabel = noticeOfProceedingService.initNoticeOfProceeding(caseData);

        assertThat(listAndLabel).doesNotContainKey("proceedingLabel");
        assertThat(listAndLabel)
            .extracting("noticeOfProceedings")
            .isEqualTo(buildExpectedNoticeOfProceedingData());
    }

    @Test
    void shouldSetHearingLabelWhenOnlyHearingDetailsExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(createHearingBookings())
            .build();

        Map<String, Object> listAndLabel = noticeOfProceedingService.initNoticeOfProceeding(caseData);

        assertThat(listAndLabel).doesNotContainKey("noticeOfProceedings");
        assertThat(listAndLabel)
            .extracting("proceedingLabel")
            .isEqualTo(buildExpectedHearingLabel());
    }

    @Test
    void shouldSetNoticeOfProceedingJudgeToAllocatedJudgeWhenUseAllocatedJudgeHasBeenSelected() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = buildJudgeAndLegalAdvisor(HIS_HONOUR_JUDGE, JUDGE_SURNAME);

        CaseData caseData = CaseData.builder()
            .allocatedJudge(buildAllocatedJudge())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .allocatedJudgeLabel("Allocated judge label")
                    .useAllocatedJudge(YES.getValue())
                    .build())
                .build())
            .build();

        NoticeOfProceedings noticeOfProceedings = noticeOfProceedingService.setNoticeOfProceedingJudge(caseData);

        assertThat(noticeOfProceedings.getJudgeAndLegalAdvisor()).isEqualTo(expectedJudgeAndLegalAdvisor);
    }

    @Test
    void shouldSetNoticeOfProceedingJudgeToTemporaryJudgeWhenUseAllocatedJudgeHasNotBeenSelected() {
        JudgeAndLegalAdvisor expectedJudgeAndLegalAdvisor = buildJudgeAndLegalAdvisor(HER_HONOUR_JUDGE, "Wilson");

        CaseData caseData = CaseData.builder()
            .allocatedJudge(buildAllocatedJudge())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Wilson")
                    .allocatedJudgeLabel("Allocated judge label")
                    .useAllocatedJudge(NO.getValue())
                    .build())
                .build())
            .build();

        NoticeOfProceedings noticeOfProceedings = noticeOfProceedingService.setNoticeOfProceedingJudge(caseData);

        assertThat(noticeOfProceedings.getJudgeAndLegalAdvisor()).isEqualTo(expectedJudgeAndLegalAdvisor);
    }

    @Test
    void shouldRetrieveExistingC6AWhenC6ANotIncludedInTemplateList() {
        List<Element<DocumentBundle>> noticeOfProceedingsBefore = generateNoticeOfProceedingBundle(List.of(C6A));
        List<Element<DocumentBundle>> noticeOfProceedingsCurrent = generateNoticeOfProceedingBundle(List.of(C6));
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6);

        List<Element<DocumentBundle>> documentBundle = noticeOfProceedingService
            .prepareNoticeOfProceedingBundle(noticeOfProceedingsCurrent, noticeOfProceedingsBefore, templatesList);

        assertThat(documentBundle).hasSize(2);
        assertThat(documentBundle.get(1).getValue().getDocument().getFilename()).isEqualTo(C6A.getDocumentTitle());
    }

    @Test
    void shouldNotRetrieveExistingDocumentsAWhenTemplateListIncludeBothC6AndC6A() {
        List<Element<DocumentBundle>> noticeOfProceedingsBefore = generateNoticeOfProceedingBundle(List.of(C6, C6A));
        List<Element<DocumentBundle>> noticeOfProceedingsCurrent = generateNoticeOfProceedingBundle(List.of(C6, C6A));
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6, C6A);

        List<Element<DocumentBundle>> documentBundle = noticeOfProceedingService
            .prepareNoticeOfProceedingBundle(noticeOfProceedingsCurrent, noticeOfProceedingsBefore, templatesList);

        assertThat(documentBundle).hasSize(2);
        assertThat(documentBundle.get(1).getValue().getDocument().getFilename()).isEqualTo(C6A.getDocumentTitle());
    }

    @Test
    void shouldGenerateAndUploadNoticeOfProceedingDocmosisDocuments() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(buildAllocatedJudge())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .proceedingTypes(List.of(NOTICE_OF_PROCEEDINGS_FOR_PARTIES, NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES))
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(HER_HONOUR_JUDGE)
                    .judgeLastName("Wilson")
                    .allocatedJudgeLabel("Allocated judge label")
                    .useAllocatedJudge(NO.getValue())
                    .build())
                .build())
            .build();

        Document document = document();

        DocmosisDocument c6Document = DocmosisDocument.builder()
            .documentTitle(C6.getDocumentTitle())
            .bytes(PDF)
            .build();

        DocmosisDocument c6aDocument = DocmosisDocument.builder()
            .documentTitle(C6.getDocumentTitle())
            .bytes(PDF)
            .build();

        DocmosisNoticeOfProceeding templateData = DocmosisNoticeOfProceeding.builder().build();

        given(noticeOfProceedingsTemplateDataGenerationService.getTemplateData(caseData))
            .willReturn(templateData);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6))
            .willReturn(c6Document);

        given(docmosisDocumentGeneratorService.generateDocmosisDocument(templateData, C6A))
            .willReturn(c6aDocument);

        given(uploadDocumentService.uploadPDF(PDF, C6.getDocumentTitle()))
            .willReturn(document);

        given(uploadDocumentService.uploadPDF(PDF, C6A.getDocumentTitle()))
            .willReturn(document);

        List<Element<DocumentBundle>> noticeOfProceedings
            = noticeOfProceedingService.uploadNoticesOfProceedings(caseData, List.of(C6, C6A));

        assertThat(noticeOfProceedings.size()).isEqualTo(2);
    }

    @Test
    void shouldReturnNoticeOfProceedingsWhenExistingOnCaseDataBefore() {
        List<Element<DocumentBundle>> noticeOfProceedingsBundle = List.of(
            element(DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("test.pdf")
                    .build())
                .build()));

        CaseData caseDataBefore = CaseData.builder()
            .noticeOfProceedingsBundle(noticeOfProceedingsBundle)
            .build();

        List<Element<DocumentBundle>> previousNoticeOfProceedings
            = noticeOfProceedingService.getPreviousNoticeOfProceedings(caseDataBefore);

        assertThat(previousNoticeOfProceedings).isEqualTo(noticeOfProceedingsBundle);
    }

    @Test
    void shouldReturnAnEmptyListIfCaseDataBeforeIsNull() {
        List<Element<DocumentBundle>> previousNoticeOfProceedings
            = noticeOfProceedingService.getPreviousNoticeOfProceedings(null);

        assertThat(previousNoticeOfProceedings).isEqualTo(List.of());
    }

    @Test
    void shouldReturnAnEmptyListWhenNoticeOfProceedingsDoNotExistOnCaseDataBefore() {
        CaseData caseData = CaseData.builder().build();
        List<Element<DocumentBundle>> previousNoticeOfProceedings =
            noticeOfProceedingService.getPreviousNoticeOfProceedings(caseData);

        assertThat(previousNoticeOfProceedings).isEqualTo(List.of());
    }

    private List<Element<DocumentBundle>> generateNoticeOfProceedingBundle(List<DocmosisTemplates> templateTypes) {
        return templateTypes.stream()
            .map(template -> element(DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename(template.getDocumentTitle())
                    .build())
                .build()))
            .collect(Collectors.toList());
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ElementUtils.wrapElements(
            createHearingBooking(now.plusDays(5), now.plusHours(6)),
            createHearingBooking(now.plusDays(2), now.plusMinutes(45)),
            createHearingBooking(now, now.plusHours(2)));
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName(JUDGE_SURNAME)
            .build();
    }

    private String buildExpectedHearingLabel() {
        return String.format("The case management hearing will be on the %s.",
            formatLocalDateTimeBaseUsingFormat(now, DATE));
    }

    private Map<String, Object> buildExpectedNoticeOfProceedingData() {
        return Map.of(
            "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder()
                .allocatedJudgeLabel("Case assigned to: His Honour Judge Davidson")
                .build()
        );
    }

    private JudgeAndLegalAdvisor buildJudgeAndLegalAdvisor(JudgeOrMagistrateTitle title, String lastName) {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(title)
            .judgeLastName(lastName)
            .build();
    }
}
