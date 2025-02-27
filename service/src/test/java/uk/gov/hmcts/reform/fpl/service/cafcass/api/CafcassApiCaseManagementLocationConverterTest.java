package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseManagementLocation;

import java.util.List;

public class CafcassApiCaseManagementLocationConverterTest extends CafcassApiConverterTestBase {

    CafcassApiCaseManagementLocationConverterTest() {
        super(new CafcassApiCaseManagementLocationConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.caseManagementLocation"));
    }

    @Test
    void shouldConvertCaseManagementLocation() {
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(CaseLocation.builder()
                .region("region").baseLocation("baseLocation").build())
            .build();

        testConvert(caseData, CafcassApiCaseData.builder()
            .caseManagementLocation(CafcassApiCaseManagementLocation.builder()
                .region("region").baseLocation("baseLocation").build())
            .build());
    }

    @Test
    void shouldReturnNullIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .caseManagementLocation(null)
            .build();

        testConvert(CaseData.builder().caseManagementLocation(null).build(), expected);
        testConvert(CaseData.builder().caseManagementLocation(CaseLocation.builder().build()).build(), expected);
    }
}
