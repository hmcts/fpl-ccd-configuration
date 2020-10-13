package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENT_KEY;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { JacksonAutoConfiguration.class, RestTemplate.class, CaseDataExtractionService.class,
    HearingVenueLookUpService.class, FixedTimeConfiguration.class, HearingBookingService.class,
    NoticeOfHearingGenerationService.class, LookupTestConfig.class, DocmosisConfiguration.class,
    NoticeOfProceedingsService.class})
class NoticeOfProceedingsServiceTest {

    @Autowired
    private NoticeOfProceedingsService noticeOfProceedingService;

    @Autowired
    private HearingBookingService hearingBookingService;

    @Autowired
    private Time time;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

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

        assertThat(listAndLabel)
            .extracting("proceedingLabel", "noticeOfProceedings")
            .containsExactly(null, buildExpectedNoticeOfProceedingData());
    }

    @Test
    void shouldSetHearingLabelWhenOnlyHearingDetailsExistOnCaseData() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(createHearingBookings())
            .build();

        Map<String, Object> listAndLabel = noticeOfProceedingService.initNoticeOfProceeding(caseData);

        assertThat(listAndLabel)
            .extracting("proceedingLabel", "noticeOfProceedings")
            .containsExactly(buildExpectedHearingLabel(), null);
    }

    @Test
    void shouldRetrieveExistingC6AWhenC6ANotIncludedInTemplateList() {
//        CaseData caseData = generateNoticeOfProceedingBundle(ImmutableList.of(C6A));
//        List<DocmosisTemplates> templatesList = ImmutableList.of(C6);
//        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
//            .getRemovedDocumentBundles(caseData, templatesList);
//
//        assertThat(removedDocuments).hasSize(1);
//
//        DocumentReference documentReference = removedDocuments.get(0).getValue().getDocument();
//
//        assertThat(documentReference.getFilename()).isEqualTo(C6A.getDocumentTitle());
    }

    @Test
    void shouldNotRetrieveExistingDocumentsAWhenTemplateListIncludeBothC6AndC6A() {
//        List<DocmosisTemplates> templatesList = ImmutableList.of(C6, C6A);
//        CaseData caseData = generateNoticeOfProceedingBundle(templatesList);
//        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
//            .getRemovedDocumentBundles(caseData, templatesList);
//
//        assertThat(removedDocuments).isEmpty();
    }

    private CaseData generateNoticeOfProceedingBundle(List<DocmosisTemplates> templateTypes) {
        return CaseData.builder()
            .noticeOfProceedingsBundle(templateTypes.stream()
                .map(docmosisDocument -> Element.<DocumentBundle>builder()
                    .id(UUID.randomUUID())
                    .value(DocumentBundle.builder()
                        .document(DocumentReference.builder()
                            .filename(docmosisDocument.getDocumentTitle())
                            .build())
                        .build())
                    .build()).collect(Collectors.toList())).build();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ElementUtils.wrapElements(
            createHearingBooking(now.plusDays(5), now.plusHours(6)),
            createHearingBooking(now.plusDays(2), now.plusMinutes(45)),
            createHearingBooking(now, now.plusHours(2)));
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE)
            .judgeLastName("Davidson")
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
}
