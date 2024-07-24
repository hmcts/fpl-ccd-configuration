package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class CafcassApiConverterTestBase {
    CafcassApiCaseDataConverter underTest;

    private CafcassApiConverterTestBase() {}
    CafcassApiConverterTestBase(CafcassApiCaseDataConverter converterUnderTest) {
        underTest = converterUnderTest;
    }

    public CafcassApiCaseData testConvert(CaseData caseData, CafcassApiCaseData expected) {
        CafcassApiCaseData actual = underTest.convert(caseData, CafcassApiCaseData.builder()).build();
        assertEquals(actual, expected);
        return actual;
    }
}
