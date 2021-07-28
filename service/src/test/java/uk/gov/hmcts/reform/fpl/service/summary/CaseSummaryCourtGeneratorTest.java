package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CourtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseSummaryCourtGeneratorTest {

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CaseSummaryCourtGenerator underTest;

    @Test
    void shouldBuildCaseSummaryWithCourtName() {
        final String courtName = "Court 1";
        final CaseData caseData = CaseData.builder().build();

        when(courtService.getCourtName(caseData)).thenReturn(courtName);

        final SyntheticCaseSummary actualCaseSummary = underTest.generate(caseData);
        final SyntheticCaseSummary expectedCaseSummary = SyntheticCaseSummary.builder()
            .caseSummaryCourtName(courtName)
            .build();

        assertThat(actualCaseSummary).isEqualTo(expectedCaseSummary);
    }
}
