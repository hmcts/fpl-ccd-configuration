package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, HearingVenueLookUpService.class})
class NoticeOfProceedingsServiceTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String COURT_CODE = "11";
    private static final String CONFIG = String.format("%s=>%s:%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL,
        COURT_CODE);
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);

    private HearingBookingService hearingBookingService = new HearingBookingService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);

    @Autowired
    private HearingVenueLookUpService hearingVenueLookUpService;

    private CommonCaseDataExtractionService commonCaseDataExtractionService = new CommonCaseDataExtractionService(
        hearingVenueLookUpService);

    private NoticeOfProceedingsService noticeOfProceedingService;

    @BeforeEach
    void setup() {
        noticeOfProceedingService = new NoticeOfProceedingsService(hearingBookingService, hmctsCourtLookupConfiguration,
            hearingVenueLookUpService, commonCaseDataExtractionService);
    }

    @Test
    void shouldRetrieveExistingC6AWhenC6ANotIncludedInTemplateList() {
        CaseData caseData = generateNoticeOfProceedingBundle(ImmutableList.of(C6A));
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6);
        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
            .getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).hasSize(1);

        DocumentReference documentReference = removedDocuments.get(0).getValue().getDocument();

        assertThat(documentReference.getFilename()).isEqualTo(C6A.getDocumentTitle());
    }

    @Test
    void shouldNotRetrieveExistingDocumentsAWhenTemplateListIncludeBothC6AndC6A() {
        List<DocmosisTemplates> templatesList = ImmutableList.of(C6, C6A);
        CaseData caseData = generateNoticeOfProceedingBundle(templatesList);
        List<Element<DocumentBundle>> removedDocuments = noticeOfProceedingService
            .getRemovedDocumentBundles(caseData, templatesList);

        assertThat(removedDocuments).isEmpty();
    }

    @Test
    void shouldApplySentenceFormattingWhenMultipleChildrenExistOnCase() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(createPopulatedChildren())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark and Jon Snow");
    }

    @Test
    void shouldNotApplySentenceFormattingWhenOnlyOneChildExistsOnCase() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(ImmutableList.of(
                Element.<Child>builder()
                    .id(UUID.randomUUID())
                    .value(Child.builder()
                        .party(ChildParty.builder()
                            .firstName("Bran")
                            .lastName("Stark")
                            .build())
                        .build())
                    .build()))
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark");
    }

    @Test
    void shouldFormatMagistrateFullNameWhenJudgeTitleIsSetToMagistrate() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(createPopulatedChildren())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(MAGISTRATES)
                    .judgeFullName("James Nelson")
                    .build())
                .proceedingTypes(emptyList())
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .build();

        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("James Nelson (JP)");
    }

    @Test
    void shouldSetJudgeTitleAndNameToEmptyStringWhenJudgeTitleAndNameIsEmpty() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(createPopulatedChildren())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                .proceedingTypes(emptyList())
                .build())
            .build();
        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("");
    }

    @Test
    void shouldReturnFirstApplicantNameWhenMultipleApplicantsArePresent() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(createPopulatedChildren())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
    }

    @Test
    void shouldMapCaseDataPropertiesToTemplatePlaceholderDataWhenCaseDataIsFullyPopulated() {
        CaseData caseData = initNoticeOfProceedingCaseData()
            .children1(createPopulatedChildren())
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(
                    CARE_ORDER,
                    EDUCATION_SUPERVISION_ORDER
                )).build())
            .build();

        Map<String, Object> templateData = noticeOfProceedingService.getNoticeOfProceedingTemplateData(caseData);
        assertThat(templateData.get("courtName")).isEqualTo("Example Court");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
        assertThat(templateData.get("orderTypes")).isEqualTo("Care order, Education supervision order");
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark and Jon Snow");
        assertThat(templateData.get("hearingDate")).isEqualTo(formatLocalDateToString(FUTURE_DATE, FormatStyle.LONG));
        assertThat(templateData.get("hearingVenue"))
            .isEqualTo("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("8:30am");
        assertThat(templateData.get("hearingTime")).isEqualTo("9:30am - 11:30am");
        assertThat(templateData.get("judgeTitleAndName")).isEqualTo("His Honour Judge Samuel Davidson");
        assertThat(templateData.get("legalAdvisorName")).isEqualTo("John Bishop");
        assertThat(templateData.get("crest")).isEqualTo(CREST.getValue());
        assertThat(templateData.get("courtseal")).isEqualTo(COURT_SEAL.getValue());
    }

    private JudgeAndLegalAdvisor createJudgeAndLegalAdvisor() {
        return JudgeAndLegalAdvisor.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Samuel Davidson")
            .legalAdvisorName("John Bishop")
            .build();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(9, 30)),
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(11, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(12, 30)),
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(13, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(15, 30)),
                    LocalDateTime.of(FUTURE_DATE, LocalTime.of(16, 0))))
                .build()
        );
    }

    private CaseData.CaseDataBuilder initNoticeOfProceedingCaseData() {
        return CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings());
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
}
