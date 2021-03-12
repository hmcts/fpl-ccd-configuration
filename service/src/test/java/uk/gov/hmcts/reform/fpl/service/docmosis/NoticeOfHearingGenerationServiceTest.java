package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
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
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
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

    private final HmctsCourtLookupConfiguration courtLookup = new LookupTestConfig().courtLookupConfiguration();
    private final Time time = new FixedTimeConfiguration().fixedDateTime(LocalDateTime.of(2021, 3, 3, 3, 3, 3));
    private final CaseDataExtractionService dataExtractionService = mock(CaseDataExtractionService.class);
    private final NoticeOfHearingGenerationService underTest = new NoticeOfHearingGenerationService(
        dataExtractionService, courtLookup, time
    );

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
                .preHearingAttendance("1:00pm")
                .hearingJudgeTitleAndName("should be removed")
                .hearingLegalAdvisorName("should also be removed")
                .build()
        );
    }

    @Test
    void shouldBuildExpectedTemplateDataWithStandardHearingType() {
        when(HEARING.getType()).thenReturn(CASE_MANAGEMENT);

        CaseData caseData = getCaseData();

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            "case management", DOCMOSIS_JUDGE_AND_LA
        );

        assertThat(templateData).isEqualTo(expectedTemplateData);
    }

    @Test
    void shouldBuildExpectedTemplateDataWithOtherHearingType() {
        when(HEARING.getType()).thenReturn(OTHER);
        when(HEARING.getTypeDetails()).thenReturn("some different type of hearing");

        CaseData caseData = getCaseData();

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            "some different type of hearing", DOCMOSIS_JUDGE_AND_LA
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

        DocmosisNoticeOfHearing templateData = underTest.getTemplateData(caseData, HEARING);

        DocmosisNoticeOfHearing expectedTemplateData = getExpectedNoticeOfHearingTemplate(
            "case management", docmosisJudgeAndLA
        );

        assertThat(templateData).isEqualTo(expectedTemplateData);

    }

    private CaseData getCaseData() {
        return getCaseData(null);
    }

    private CaseData getCaseData(Judge allocatedJudge) {
        return CaseData.builder()
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
            .children(DOCMOSIS_CHILDREN)
            .hearingBooking(
                DocmosisHearingBooking.builder()
                    .hearingDate("11 March 2021")
                    .hearingTime("2:00pm - 4:30pm")
                    .hearingVenue("somewhere")
                    .preHearingAttendance("1:00pm")
                    .hearingType(hearingType)
                    .build())
            .judgeAndLegalAdvisor(judgeAndLA)
            .postingDate("3 March 2021")
            .additionalNotes(NOTES)
            .courtName(DEFAULT_LA_COURT)
            .courtseal("[userImage:familycourtseal.png]")
            .crest("[userImage:crest.png]")
            .build();
    }
}
