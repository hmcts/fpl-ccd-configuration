package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.CREST;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NoticeOfHearingGenerationService.class})
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CaseDataExtractionService.class, HearingVenueLookUpService.class,
    LookupTestConfig.class, HearingBookingService.class, FixedTimeConfiguration.class
})
class NoticeOfHearingGenerationServiceTest {

    private static final String HEARING_DATE_AND_TIME_FORMAT = "d MMMM, h:mma";
    private static final String HEARING_TIME_FORMAT = "h:mma";

    @Autowired
    NoticeOfHearingGenerationService service;

    @MockBean
    DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    UploadDocumentService uploadDocumentService;

    @Test
    void shouldBuildExpectedTemplateDataWithHearingTimeWhenHearingStartAndEndDateDiffer() {
        LocalDateTime now = LocalDateTime.now();

        CaseData caseData = buildCaseData(now.toLocalDate());
        HearingBooking hearingBooking = buildHearingBooking(now, now.plusDays(1));
        final DocmosisNoticeOfHearing actualDocmosisNoticeOfHearing = service.getTemplateData(caseData, hearingBooking);

        DocmosisNoticeOfHearing.DocmosisNoticeOfHearingBuilder docmosisNoticeOfHearingBuilder
            = getDocmosisNoticeOfHearingBuilder(now.toLocalDate())
            .hearingBooking(getDocmosisHearingBookingBuilder()
            .hearingTime(String.format("%s - %s",
                formatLocalDateTime(hearingBooking.getStartDate(), HEARING_DATE_AND_TIME_FORMAT),
                formatLocalDateTime(hearingBooking.getEndDate(), HEARING_DATE_AND_TIME_FORMAT)))
            .preHearingAttendance(formatLocalDateTime(now.minusHours(1), DATE_TIME))
            .hearingDate("")
            .build());

        DocmosisNoticeOfHearing expectedDocmosisNoticeOfHearing = docmosisNoticeOfHearingBuilder.build();

        assertThat(actualDocmosisNoticeOfHearing).isEqualToComparingFieldByField(expectedDocmosisNoticeOfHearing);
    }

    @Test
    void shouldBuildExpectedTemplateDataWithHearingDateAndTimeWhenHearingStartAndEndDateAreTheSame() {
        LocalDateTime now = LocalDateTime.now();

        CaseData caseData = buildCaseData(now.toLocalDate());
        HearingBooking hearingBooking = buildHearingBooking(now, now);
        final DocmosisNoticeOfHearing actualDocmosisNoticeOfHearing = service.getTemplateData(caseData, hearingBooking);
        DocmosisNoticeOfHearing.DocmosisNoticeOfHearingBuilder docmosisNoticeOfHearingBuilder
            = getDocmosisNoticeOfHearingBuilder(now.toLocalDate())
            .hearingBooking(getDocmosisHearingBookingBuilder()
            .hearingDate(formatLocalDateTime(now, DATE))
            .preHearingAttendance(formatLocalDateTime(now.minusHours(1), "h:mma"))
            .hearingTime(String.format("%s - %s",
                formatLocalDateTime(now, HEARING_TIME_FORMAT),
                formatLocalDateTime(now, HEARING_TIME_FORMAT)))
            .build());

        DocmosisNoticeOfHearing expectedDocmosisNoticeOfHearing = docmosisNoticeOfHearingBuilder.build();

        assertThat(actualDocmosisNoticeOfHearing).isEqualToComparingFieldByField(expectedDocmosisNoticeOfHearing);
    }

    private DocmosisNoticeOfHearing.DocmosisNoticeOfHearingBuilder getDocmosisNoticeOfHearingBuilder(
        LocalDate dateOfBirth) {
        return DocmosisNoticeOfHearing.builder()
            .familyManCaseNumber("12345")
            .courtName("Family Court")
            .children(getExpectedDocmosisChildren(dateOfBirth))
            .hearingBooking(getDocmosisHearingBookingBuilder().build())
            .judgeAndLegalAdvisor(getExpectedDocmosisJudgeAndLegalAdvisor())
            .postingDate(formatLocalDateToString(LocalDate.now(), DATE))
            .additionalNotes("additional note")
            .courtseal(COURT_SEAL.getValue())
            .crest(CREST.getValue());
    }

    private HearingBooking buildHearingBooking(LocalDateTime startDate, LocalDateTime endDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .venue("Venue")
            .endDate(endDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HER_HONOUR_JUDGE)
                .judgeLastName("Law")
                .legalAdvisorName("Watson")
                .build())
            .additionalNotes("additional note")
            .build();
    }

    private List<DocmosisChild> getExpectedDocmosisChildren(LocalDate dateOfBirth) {
        return List.of(
            DocmosisChild.builder()
                .name("Tom Stevens")
                .dateOfBirth(formatLocalDateToString(dateOfBirth, FormatStyle.LONG))
                .gender("Male")
                .build(),
            DocmosisChild.builder()
                .name("Sarah Stevens")
                .dateOfBirth(formatLocalDateToString(dateOfBirth.minusDays(2), FormatStyle.LONG))
                .gender("Female")
                .build());
    }

    private DocmosisJudgeAndLegalAdvisor getExpectedDocmosisJudgeAndLegalAdvisor() {
        return DocmosisJudgeAndLegalAdvisor.builder()
            .legalAdvisorName("Watson")
            .judgeTitleAndName("Her Honour Judge Law")
            .build();
    }

    private DocmosisHearingBooking.DocmosisHearingBookingBuilder getDocmosisHearingBookingBuilder() {
        return DocmosisHearingBooking.builder()
            .hearingType(CASE_MANAGEMENT.getLabel().toLowerCase())
            .hearingVenue("Crown Building, Aberdare Hearing Centre, Aberdare, CF44 7DW");
    }

    private CaseData buildCaseData(LocalDate dateOfBirth) {
        return CaseData.builder()
            .familyManCaseNumber("12345")
            .caseLocalAuthority("example")
            .children1(wrapElements(
                Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Tom")
                        .lastName("Stevens")
                        .dateOfBirth(dateOfBirth)
                        .gender("Male")
                        .build())
                    .build(),
                Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Sarah")
                        .lastName("Stevens")
                        .dateOfBirth(dateOfBirth.minusDays(2))
                        .gender("Female")
                        .build())
                    .build()
            )).build();
    }

    private String formatLocalDateTime(LocalDateTime dateTime, String format) {
        return formatLocalDateTimeBaseUsingFormat(dateTime, format);
    }
}
