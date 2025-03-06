package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ProceedingStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiProceeding;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

public class CafcassApiPreviousProceedingsConverterTest extends CafcassApiConverterTestBase {
    CafcassApiPreviousProceedingsConverterTest() {
        super(new CafcassApiPreviousProceedingsConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.proceeding"));
    }

    @Test
    void shouldConvertPreviousProceedings() {
        Proceeding additionalProceeding = Proceeding.builder()
            .proceedingStatus(ProceedingStatus.PREVIOUS)
            .caseNumber("123")
            .started("2023-05-24")
            .ended("2023-05-24")
            .guardian("John Smith")
            .sameGuardianNeeded(YesNo.NO)
            .build();
        Proceeding additionalEmptyProceeding = Proceeding.builder().build();

        Proceeding proceeding = Proceeding.builder()
            .proceedingStatus(ProceedingStatus.ONGOING)
            .caseNumber("1234567898765432")
            .started("2023-05-24")
            .ordersMade("C20, C8, etc.")
            .judge("District Judge Martin Brown")
            .children("Joe Bloggs, Jane Bloggs")
            .guardian("John Smith")
            .sameGuardianNeeded(YesNo.YES)
            .sameGuardianDetails("Do not need the same guardian for x and y reasons.")
            .build();


        CaseData caseData = CaseData.builder()
            .proceedings(wrapElementsWithUUIDs(proceeding, additionalProceeding, additionalEmptyProceeding))
            .build();

        List<CafcassApiProceeding> expectedList = List.of(
            CafcassApiProceeding.builder()
                .proceedingStatus("Ongoing")
                .caseNumber("1234567898765432")
                .started("2023-05-24")
                .ordersMade("C20, C8, etc.")
                .judge("District Judge Martin Brown")
                .children("Joe Bloggs, Jane Bloggs")
                .guardian("John Smith")
                .sameGuardianNeeded(true)
                .sameGuardianDetails("Do not need the same guardian for x and y reasons.")
                .build(),
            CafcassApiProceeding.builder()
                .proceedingStatus("Previous")
                .caseNumber("123")
                .started("2023-05-24")
                .ended("2023-05-24")
                .guardian("John Smith")
                .sameGuardianNeeded(false)
                .build()
        );

        testConvert(caseData, CafcassApiCaseData.builder().previousProceedings(expectedList).build());
    }

    @Test
    void shouldReturnEmptyListIfNull() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().previousProceedings(List.of()).build();

        testConvert(CaseData.builder().proceedings(null).build(), expected);
        testConvert(CaseData.builder().proceedings(List.of()).build(), expected);
    }
}
