package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FactorsParenting;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFactorsParenting;

public class CafcassApiFactorsParentingConverterTest extends CafcassApiConverterTestBase {
    CafcassApiFactorsParentingConverterTest() {
        super(new CafcassApiFactorsParentingConverter());
    }

    @Test
    void shouldConvertFactorsParenting() {
        CaseData caseData = CaseData.builder()
            .factorsParenting(FactorsParenting.builder()
                .alcoholDrugAbuse(YesNo.YES.getValue())
                .alcoholDrugAbuseReason("alcoholDrugAbuseReason")
                .domesticViolence(YesNo.YES.getValue())
                .domesticViolenceReason("domesticViolenceReason")
                .anythingElse(YesNo.YES.getValue())
                .anythingElseReason("anythingElseReason")
                .build())
            .build();

        testConvert(caseData, CafcassApiCaseData.builder()
            .factorsParenting(CafcassApiFactorsParenting.builder()
                .alcoholDrugAbuse(true)
                .alcoholDrugAbuseReason("alcoholDrugAbuseReason")
                .domesticViolence(true)
                .domesticViolenceReason("domesticViolenceReason")
                .anythingElse(true)
                .anythingElseReason("anythingElseReason")
                .build())
            .build());
    }

    @Test
    void shouldReturnEmptyIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .factorsParenting(null)
            .build();
        testConvert(CaseData.builder().factorsParenting(null).build(), expected);
        testConvert(CaseData.builder().factorsParenting(FactorsParenting.builder().build()).build(), expected);
    }
}
