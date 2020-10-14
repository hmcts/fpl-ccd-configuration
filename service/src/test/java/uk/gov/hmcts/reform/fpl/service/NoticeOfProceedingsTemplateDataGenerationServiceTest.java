package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedApplicants;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, LookupTestConfig.class,
    HearingVenueLookUpService.class, CaseDataExtractionService.class, FixedTimeConfiguration.class,
    NoticeOfProceedingsTemplateDataGenerationService.class
})
class NoticeOfProceedingsTemplateDataGenerationServiceTest {

    @Autowired
    private Time time;

    @Autowired
    private NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;

    private LocalDate futureDate;

    @BeforeEach
    void setup() {
        futureDate = time.now().toLocalDate().plusDays(1);
    }

    @Test
    void shouldApplySentenceFormattingWhenMultipleChildrenExistOnCase() {
        CaseData caseData = prepareCaseData()
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);
        assertThat(templateData.getChildrenNames()).isEqualTo("Bran Stark, Sansa Stark and Jon Snow");
    }

    @Test
    void shouldNotApplySentenceFormattingWhenOnlyOneChildExistsOnCase() {
        CaseData caseData = prepareCaseData()
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
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);
        assertThat(templateData.getChildrenNames()).isEqualTo("Bran Stark");
    }

    @Test
    void shouldFormatMagistrateFullNameWhenJudgeTitleIsSetToMagistrate() {
        CaseData caseData = prepareCaseData()
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(MAGISTRATES)
                    .judgeFullName("James Nelson")
                    .build())
                .proceedingTypes(emptyList())
                .build())
            .build();

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);
    }

    @Test
    void shouldMapCaseDataPropertiesToTemplatePlaceholderDataWhenCaseDataIsFullyPopulated() {
        CaseData caseData = prepareCaseData()
            .noticeOfProceedings(NoticeOfProceedings.builder()
                .judgeAndLegalAdvisor(createJudgeAndLegalAdvisor())
                .proceedingTypes(emptyList())
                .build())
            .build();

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);

        DocmosisNoticeOfProceeding expectedData = DocmosisNoticeOfProceeding.builder()
            .courtName("Family Court")
            .familyManCaseNumber("123")
            .applicantName("Bran Stark")
            .orderTypes("Care order")
            .childrenNames("Bran Stark, Sansa Stark and Jon Snow")
            .hearingBooking(DocmosisHearingBooking.builder()
                .hearingDate(formatLocalDateToString(futureDate, FormatStyle.LONG))
                .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW")
                .preHearingAttendance("8:30am")
                .hearingTime("9:30am - 11:30am")
                .build())
            .todaysDate(formatLocalDateToString(time.now().toLocalDate(), FormatStyle.LONG))
            .crest("[userImage:crest.png]")
            .courtseal("[userImage:familycourtseal.png]")
            .build();

        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
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
                    LocalDateTime.of(futureDate, LocalTime.of(9, 30)),
                    LocalDateTime.of(futureDate, LocalTime.of(11, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(futureDate, LocalTime.of(12, 30)),
                    LocalDateTime.of(futureDate, LocalTime.of(13, 30))))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(
                    LocalDateTime.of(futureDate, LocalTime.of(15, 30)),
                    LocalDateTime.of(futureDate, LocalTime.of(16, 0))))
                .build()
        );
    }

    private CaseData.CaseDataBuilder prepareCaseData() {
        return CaseData.builder()
            .caseLocalAuthority("example")
            .familyManCaseNumber("123")
            .orders(Orders.builder()
                .orderType(ImmutableList.of(CARE_ORDER)).build())
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings())
            .children1(createPopulatedChildren(time.now().toLocalDate()));
    }
}
