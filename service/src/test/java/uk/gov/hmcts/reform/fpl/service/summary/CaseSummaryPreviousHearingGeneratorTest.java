package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CaseSummaryPreviousHearingGeneratorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2012, 10, 12, 13, 20, 44);

    private final Time time = mock(Time.class);

    private final CaseSummaryPreviousHearingGenerator underTest =
        new CaseSummaryPreviousHearingGenerator(time);

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
    void testSingleHearingsWithNoSealedCmo() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .type(HearingType.CASE_MANAGEMENT)
                    .startDate(NOW.minusHours(1))
                    .endDate(NOW.minusSeconds(1))
                    .build())
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasPreviousHearing("Yes")
            .caseSummaryPreviousHearingDate(NOW.toLocalDate())
            .caseSummaryPreviousHearingType("Case management")
            .build());
    }
}
