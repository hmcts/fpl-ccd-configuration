package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;

class CaseSummaryFinalHearingGeneratorTest {

    private final CaseSummaryFinalHearingGenerator underTest = new CaseSummaryFinalHearingGenerator();

    @Test
    void testNoHearings() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testEmptyHearings() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(emptyList())
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testNoFinalHearings() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(Element.<HearingBooking>builder()
                .value(HearingBooking.builder()
                    .type(CASE_MANAGEMENT)
                    .build())
                .build()))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testHasFinalHearing() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .hearingDetails(List.of(
                Element.<HearingBooking>builder()
                    .value(HearingBooking.builder()
                        .type(CASE_MANAGEMENT)
                        .build())
                    .build(),
                Element.<HearingBooking>builder()
                    .value(HearingBooking.builder()
                        .type(FINAL)
                        .startDate(LocalDateTime.of(2012,1,1,12,34))
                        .build())
                    .build())
                ).build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasFinalHearing("Yes")
            .caseSummaryFinalHearingDate(LocalDate.of(2012,1,1))
            .build());
    }
}
