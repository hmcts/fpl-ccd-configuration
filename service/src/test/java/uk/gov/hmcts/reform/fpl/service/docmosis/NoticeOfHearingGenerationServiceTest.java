package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.enums.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingBooking;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisJudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfHearingVacated;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FACT_FINDING;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider.RELIST_ACTION_NOT_RELISTED;
import static uk.gov.hmcts.reform.fpl.service.email.content.HearingVacatedEmailContentProvider.RELIST_ACTION_RELISTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class NoticeOfHearingGenerationServiceTest {

    private static final String NOTES = "Notes";
    private static final String FAMILY_MAN_CASE_NUMBER = "12345";

    private static final HearingBooking HEARING = mock(HearingBooking.class);
    private static final List<Element<Child>> CHILDREN = wrapElements(mock(Child.class));
    private static final JudgeAndLegalAdvisor JUDGE_AND_LA = mock(JudgeAndLegalAdvisor.class);

    private static final List<DocmosisChild> DOCMOSIS_CHILDREN = List.of(
        DocmosisChild.builder()
            .name("Tom Stevens")
            .dateOfBirth("10 March 2012")
            .gender("Male")
            .build()
    );
    private static final DocmosisJudgeAndLegalAdvisor DOCMOSIS_JUDGE_AND_LA = DocmosisJudgeAndLegalAdvisor.builder()
        .judgeTitleAndName("Her Honour Judge Law")
        .legalAdvisorName("Watson")
        .build();
    private static final long CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

    private final CourtService courtService = mock(CourtService.class);
    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 3, 3, 3, 3));
    private final CaseDataExtractionService dataExtractionService = mock(CaseDataExtractionService.class);

    private final NoticeOfHearingGenerationService underTest = new NoticeOfHearingGenerationService(
        dataExtractionService, courtService, time);

    @BeforeEach
    void mocks() {
        when(JUDGE_AND_LA.isUsingAllocatedJudge()).thenReturn(false);

        when(HEARING.getAdditionalNotes()).thenReturn(NOTES);
        when(HEARING.getJudgeAndLegalAdvisor()).thenReturn(JUDGE_AND_LA);

        when(dataExtractionService.getChildrenDetails(CHILDREN)).thenReturn(DOCMOSIS_CHILDREN);
        when(dataExtractionService.getJudgeAndLegalAdvisor(JUDGE_AND_LA)).thenReturn(DOCMOSIS_JUDGE_AND_LA);
        when(dataExtractionService.getHearingBookingData(HEARING)).thenReturn(
            DocmosisHearingBooking.builder()
                .hearingDate("11 March 2021")
                .hearingTime("2:00pm - 4:30pm")
                .hearingVenue("somewhere")
                .hearingAttendance("In person, remote - video call")
                .hearingAttendanceDetails("Join: https://remote-hearing.gov.uk/123")
                .preHearingAttendance("20 minutes before hearing")
                .hearingJudgeTitleAndName("should be removed")
                .hearingLegalAdvisorName("should also be removed")
                .build());

        when(courtService.getCourtName(any())).thenReturn(COURT_NAME);
    }

    @Test
    void shouldBuildExpectedTemplateDataWithStandardHearingType() {
        when(HEARING.getType()).thenReturn(CASE_MANAGEMENT);

        CaseData caseData = getCaseData();

        when(courtService.getCourtSeal(caseData, SEALED))
                .thenReturn(COURT_SEAL.getValue(caseData.getImageLanguage()));

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            "case management", DOCMOSIS_JUDGE_AND_LA
        );

        assertThat(templateData).isEqualTo(expectedTemplateData);
    }

    @Test
    void shouldBuildExpectedTemplateDataWithHearingType() {
        when(HEARING.getType()).thenReturn(FACT_FINDING);

        CaseData caseData = getCaseData();

        when(courtService.getCourtSeal(caseData, SEALED))
                .thenReturn(COURT_SEAL.getValue(caseData.getImageLanguage()));

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            FACT_FINDING.getLabel().toLowerCase(), DOCMOSIS_JUDGE_AND_LA
        );

        assertThat(templateData).isEqualTo(expectedTemplateData);
    }

    @Test
    void shouldBuildExpectedTemplateDataWithAllocatedJudge() {
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .build();

        JudgeAndLegalAdvisor selectedJudgeAndLA = JudgeAndLegalAdvisor.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Judy")
            .build();

        DocmosisJudgeAndLegalAdvisor docmosisJudgeAndLA = DocmosisJudgeAndLegalAdvisor.builder()
            .judgeTitleAndName("Her Honour Judge Judy")
            .legalAdvisorName("Watson")
            .build();

        when(HEARING.getType()).thenReturn(CASE_MANAGEMENT);
        when(JUDGE_AND_LA.isUsingAllocatedJudge()).thenReturn(true);
        when(dataExtractionService.getJudgeAndLegalAdvisor(selectedJudgeAndLA)).thenReturn(docmosisJudgeAndLA);


        CaseData caseData = getCaseData(allocatedJudge);

        when(courtService.getCourtSeal(caseData, SEALED))
                .thenReturn(COURT_SEAL.getValue(caseData.getImageLanguage()));

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            "case management", docmosisJudgeAndLA
        );

        assertThat(templateData).isEqualTo(expectedTemplateData);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldBuildExpectedHearingVacatedTemplateData(boolean relist) {
        LocalDate vacatedDate = LocalDate.of(2024, 06, 07);

        CaseData caseData = CaseData.builder()
            .id(CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .hearingDetails(wrapElements(HEARING))
            .build();

        when(HEARING.getType()).thenReturn(CASE_MANAGEMENT);
        when(HEARING.getCancellationReason()).thenReturn(HearingCancellationReason.OT1.name());
        when(HEARING.getVacatedDate()).thenReturn(vacatedDate);

        when(courtService.getCourtSeal(caseData, SEALED))
            .thenReturn(COURT_SEAL.getValue(caseData.getImageLanguage()));

        DocmosisNoticeOfHearingVacated templateData =
            underTest.getHearingVacatedTemplateData(caseData, HEARING, relist);

        DocmosisNoticeOfHearingVacated expectedTemplateData = DocmosisNoticeOfHearingVacated.builder()
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .hearingBooking(
                DocmosisHearingBooking.builder()
                    .hearingDate("11 March 2021")
                    .hearingTime("2:00pm - 4:30pm")
                    .hearingVenue("somewhere")
                    .hearingAttendance("In person, remote - video call")
                    .hearingAttendanceDetails("Join: https://remote-hearing.gov.uk/123")
                    .preHearingAttendance("20 minutes before hearing")
                    .hearingType("case management")
                    .build())
            .vacatedDate("7 June 2024")
            .vacatedReason(HearingCancellationReason.OT1.getLabel())
            .relistAction(relist ? RELIST_ACTION_RELISTED : RELIST_ACTION_NOT_RELISTED)
            .crest("[userImage:crest.png]")
            .build();

        assertThat(templateData).isEqualTo(expectedTemplateData);

    }

    private CaseData getCaseData() {
        return getCaseData(null);
    }

    private CaseData getCaseData(Judge allocatedJudge) {
        return CaseData.builder()
            .id(CASE_NUMBER)
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .children1(CHILDREN)
            .allocatedJudge(allocatedJudge)
            .judgeAndLegalAdvisor(JUDGE_AND_LA)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .hearingDetails(wrapElements(HEARING))
            .build();
    }

    private DocmosisNoticeOfHearing getExpectedNoticeOfHearingTemplate(String hearingType,
                                                                       DocmosisJudgeAndLegalAdvisor judgeAndLA) {
        return DocmosisNoticeOfHearing.builder()
            .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
            .ccdCaseNumber(FORMATTED_CASE_NUMBER)
            .children(DOCMOSIS_CHILDREN)
            .hearingBooking(
                DocmosisHearingBooking.builder()
                    .hearingDate("11 March 2021")
                    .hearingTime("2:00pm - 4:30pm")
                    .hearingVenue("somewhere")
                    .hearingAttendance("In person, remote - video call")
                    .hearingAttendanceDetails("Join: https://remote-hearing.gov.uk/123")
                    .preHearingAttendance("20 minutes before hearing")
                    .hearingType(hearingType)
                    .build())
            .judgeAndLegalAdvisor(judgeAndLA)
            .postingDate("3 March 2021")
            .additionalNotes(NOTES)
            .courtName(DEFAULT_LA_COURT)
            .courtseal("[userImage:familycourtseal.png]")
            .isHighCourtCase(false)
            .crest("[userImage:crest.png]")
            .build();
    }
}
