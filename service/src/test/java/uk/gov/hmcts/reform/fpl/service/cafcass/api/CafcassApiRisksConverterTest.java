package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRisk;

import java.util.List;

public class CafcassApiRisksConverterTest extends CafcassApiConverterTestBase {
    CafcassApiRisksConverterTest() {
        super(new CafcassApiRisksConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.risks"));
    }

    @Test
    void shouldConvertRisk() {
        CaseData caseData = CaseData.builder()
            .risks(Risks.builder()
                .neglect(YesNo.YES.toString())
                .neglectOccurrences(List.of("Future risk of harm"))
                .sexualAbuse(YesNo.YES.toString())
                .sexualAbuseOccurrences(List.of("Past harm"))
                .physicalHarm(YesNo.YES.toString())
                .physicalHarmOccurrences(List.of("Future risk of harm", "Past harm"))
                .emotionalHarm(YesNo.YES.toString())
                .emotionalHarmOccurrences(List.of("Future risk of harm", "Past harm"))
                .build())
            .build();

        testConvert(caseData, CafcassApiCaseData.builder()
            .risks(CafcassApiRisk.builder()
                .neglectOccurrences(List.of("Future risk of harm"))
                .sexualAbuseOccurrences(List.of("Past harm"))
                .physicalHarmOccurrences(List.of("Future risk of harm", "Past harm"))
                .emotionalHarmOccurrences(List.of("Future risk of harm", "Past harm"))
                .build())
            .build());
    }

    @Test
    void shouldReturnEmptyObjectIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().risks(null).build();

        testConvert(CaseData.builder().risks(null).build(), expected);
        testConvert(CaseData.builder().risks(Risks.builder().build()).build(), expected);
    }
}
