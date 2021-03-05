package uk.gov.hmcts.reform.fpl.service.summary;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CaseSummaryMessagesGeneratorTest {

    private final CaseSummaryMessagesGenerator underTest = new CaseSummaryMessagesGenerator();

    @Test
    void testNoMessages() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testEmptyMessages() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder().judicialMessages(emptyList()).build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.emptySummary());
    }

    @Test
    void testWithChildren() {
        SyntheticCaseSummary actual = underTest.generate(CaseData.builder()
            .judicialMessages(List.of(
                element(mock(JudicialMessage.class)),
                element(mock(JudicialMessage.class))
            ))
            .build());

        assertThat(actual).isEqualTo(SyntheticCaseSummary.builder()
            .caseSummaryHasUnresolvedMessages("Yes")
            .build());
    }

}
