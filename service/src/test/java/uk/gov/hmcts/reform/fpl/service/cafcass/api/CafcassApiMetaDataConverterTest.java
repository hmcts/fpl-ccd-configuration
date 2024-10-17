package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CONTACT_WITH_CHILD_IN_CARE;

public class CafcassApiMetaDataConverterTest extends CafcassApiConverterTestBase {
    CafcassApiMetaDataConverterTest() {
        super(new CafcassApiMetaDataConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of(
            "data.familyManCaseNumber", "data.dateSubmitted", "data.ordersSolicitor", "data.orders", "data.dateOfIssue",
            "data.isLocalAuthority", "data.relatingLA"));
    }

    @Test
    void shouldConvertC1MetaData() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("familyManCaseNumber")
            .dateSubmitted(LocalDate.of(2024, 1, 1))
            .orders(Orders.builder()
                .orderType(List.of(CONTACT_WITH_CHILD_IN_CARE))
                .build())
            .dateOfIssue(LocalDate.of(2024, 2, 1))
            .isLocalAuthority(YesNo.NO)
            .relatingLA("LA1")
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .familyManCaseNumber("familyManCaseNumber")
            .dateSubmitted(LocalDate.of(2024, 1, 1))
            .applicationType("C1")
            .ordersSought(List.of(CONTACT_WITH_CHILD_IN_CARE))
            .dateOfCourtIssue(LocalDate.of(2024, 2, 1))
            .citizenIsApplicant(true)
            .applicantLA(null)
            .respondentLA("LA1")
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldConvertC110aMetaData() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("familyManCaseNumber")
            .dateSubmitted(LocalDate.of(2024, 1, 1))
            .orders(Orders.builder()
                .orderType(List.of(CARE_ORDER))
                .build())
            .dateOfIssue(LocalDate.of(2024, 2, 1))
            .isLocalAuthority(YesNo.YES)
            .caseLocalAuthority("LA1")
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .familyManCaseNumber("familyManCaseNumber")
            .dateSubmitted(LocalDate.of(2024, 1, 1))
            .applicationType("C110A")
            .ordersSought(List.of(CARE_ORDER))
            .dateOfCourtIssue(LocalDate.of(2024, 2, 1))
            .citizenIsApplicant(false)
            .applicantLA("LA1")
            .respondentLA(null)
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldConvertIfAllFieldsAreEmptyOrNull() {
        testConvert(CaseData.builder().build(), CafcassApiCaseData.builder().applicationType("C110A").build());
    }
}
