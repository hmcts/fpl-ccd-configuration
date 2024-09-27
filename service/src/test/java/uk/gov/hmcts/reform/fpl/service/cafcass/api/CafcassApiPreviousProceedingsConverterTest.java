package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiProceeding;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiPreviousProceedingsConverterTest extends CafcassApiConverterTestBase {
    CafcassApiPreviousProceedingsConverterTest() {
        super(new CafcassApiPreviousProceedingsConverter());
    }

    @Test
    void shouldConvertPreviousProceedings() {
        Proceeding additionalProceeding = Proceeding.builder()
            .proceedingStatus("Previous")
            .caseNumber("123")
            .sameGuardianNeeded(YesNo.NO.toString())
            .build();
        Proceeding additionalEmptyProceeding = Proceeding.builder().build();

        Proceeding proceeding = Proceeding.builder()
            .proceedingStatus("Ongoing")
            .caseNumber("1234567898765432")
            .started("2023-05-24")
            .ended("2023-05-24")
            .ordersMade("C20, C8, etc.")
            .judge("District Judge Martin Brown")
            .children("Joe Bloggs, Jane Bloggs")
            .guardian("John Smith")
            .sameGuardianNeeded(YesNo.YES.toString())
            .sameGuardianDetails("Do not need the same guardian for x and y reasons.")
            .additionalProceedings(wrapElements(additionalProceeding, additionalEmptyProceeding))
            .build();


        CaseData caseData = CaseData.builder()
            .proceeding(proceeding)
            .build();

        List<CafcassApiProceeding> expectedList = List.of(
            CafcassApiProceeding.builder()
                .proceedingStatus("Ongoing")
                .caseNumber("1234567898765432")
                .started("2023-05-24")
                .ended("2023-05-24")
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
                .sameGuardianNeeded(false)
                .build()
        );

        testConvert(caseData, CafcassApiCaseData.builder().previousProceedings(expectedList).build());
    }

    @Test
    void shouldReturnEmptyListIfNull() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().previousProceedings(List.of()).build();
        testConvert(CaseData.builder().proceeding(null).build(), expected);
    }
}
