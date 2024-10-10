package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.summary.NextHearingDetails;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CaseSummaryNextHearingGeneratorTest {

    private static final String JUDGE_EMAIL_ADDRESS = "judge@email.address";
    private static final String HEARING_JUDGE_EMAIL_ADDRESS = "judgehearing@email.address";
    private static final JudgeOrMagistrateTitle JUDGE_TITLE = JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE;
    private static final JudgeOrMagistrateTitle HEARING_JUDGE_TITLE = JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
    private static final String JUDGE_LAST_NAME = "judge last name";
    private static final String HEARING_JUDGE_LAST_NAME = " hearing judge last name";
    private static final String JUDGE_LABEL = "judge label";
    private static final String HEARING_JUDGE_LABEL = "hearing judge label";
    private static final Judge ALLOCATED_JUDGE = Judge.builder()
        .judgeEmailAddress(JUDGE_EMAIL_ADDRESS)
        .judgeTitle(JUDGE_TITLE)
        .judgeLastName(JUDGE_LAST_NAME)
        .build();

    private final Time time = mock(Time.class);

    CaseSummaryNextHearingGenerator underTest = new CaseSummaryNextHearingGenerator(time);

    private static final LocalDateTime NOW = LocalDateTime.of(2012, 10, 12, 13, 20, 44);
    private static final UUID CASE_MANAGEMENT_ORDER_ID = UUID.randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final UUID ANOTHER_CMO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @Test
    void testNoHearings() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testSingleHearingsWithNoDraftCmo() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testSingleHearingsWithNonMatchingDraftCmo() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                    .build())
            )).draftUploadedCMOs(List.of(
                element(ANOTHER_CMO_ID,
                    HearingOrder.builder()
                        .order(DOCUMENT_REFERENCE)
                        .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testSingleHearingsWithAllocatedJudgeIgnored() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .build())
            )).allocatedJudge(ALLOCATED_JUDGE)
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testSingleHearingsWithAllocatedJudgeAndEmptyInfoOnHearingJudge() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
                    .build())
            )).allocatedJudge(ALLOCATED_JUDGE)
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testSingleHearingsWithAllocatedJudgeSameAsHearingJudge() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .hearingJudgeLabel(JUDGE_LABEL)
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeTitle(JUDGE_TITLE)
                        .judgeLastName(JUDGE_LAST_NAME)
                        .judgeEmailAddress(JUDGE_EMAIL_ADDRESS)
                        .build())
                    .build())
            )).allocatedJudge(ALLOCATED_JUDGE)
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testSingleHearingsWithAllocatedJudgeDiffenentThanHearingJudge() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW.plusMinutes(10))
                    .hearingJudgeLabel(HEARING_JUDGE_LABEL)
                    .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                        .judgeTitle(HEARING_JUDGE_TITLE)
                        .judgeLastName(HEARING_JUDGE_LAST_NAME)
                        .judgeEmailAddress(HEARING_JUDGE_EMAIL_ADDRESS)
                        .build())
                    .build())
            )).allocatedJudge(ALLOCATED_JUDGE)
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .caseSummaryNextHearingJudge(HEARING_JUDGE_LABEL)
            .caseSummaryNextHearingEmailAddress(HEARING_JUDGE_EMAIL_ADDRESS)
            .build());
    }

    @Test
    void testSingleHearingsWithMatchingDraftCmo() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW)
                    .caseManagementOrderId(CASE_MANAGEMENT_ORDER_ID)
                    .build())
            )).draftUploadedCMOs(List.of(
                element(CASE_MANAGEMENT_ORDER_ID,
                    HearingOrder.builder()
                        .order(DOCUMENT_REFERENCE)
                        .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .caseSummaryNextHearingCMO(DOCUMENT_REFERENCE)
            .build());
    }

    @Test
    void testMultipleHearingsSelectsLatestInPast() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.FURTHER_CASE_MANAGEMENT)
                    .startDate(NOW.minusDays(1))
                    .endDate(NOW.minusSeconds(1))
                    .build()),
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW)
                    .endDate(NOW)
                    .build()),
                element(HearingBooking.builder()
                    .type(HearingType.ISSUE_RESOLUTION)
                    .startDate(NOW.plusSeconds(1))
                    .endDate(NOW.plusMinutes(10))
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasNextHearing("Yes")
            .caseSummaryNextHearingDate(NOW.toLocalDate())
            .caseSummaryNextHearingDateTime(NOW)
            .nextHearingDetails(NextHearingDetails.builder()
                .hearingDateTime(NOW)
                .build())
            .caseSummaryNextHearingType("Case management")
            .build());
    }

    @Test
    void testNoHearingsIfAllInPast() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW.minusDays(1))
                    .endDate(NOW.minusDays(1))
                    .build()),
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW.minusSeconds(1))
                    .endDate(NOW.minusSeconds(1))
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }
}
