package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiApplicantsConverterTest extends CafcassApiConverterTestBase {

    CafcassApiApplicantsConverterTest() {
        super(new CafcassApiApplicantsConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.localAuthorities"));
    }

    @Test
    void shouldConvertAllApplicant() {
        LocalAuthority la1 = LocalAuthority.builder()
            .name("Applicant 1 name")
            .email("applicant1@test.com")
            .phone("0123456789")
            .address(getTestAddress("applicant 1 address"))
            .designated(YesNo.YES.getValue())
            .colleagues(wrapElements(getTestColleague(1)))
            .build();
        Element<LocalAuthority> la1Element = element(la1);

        LocalAuthority la2 = LocalAuthority.builder()
            .name("Applicant 2 name")
            .email("applicant2@test.com")
            .phone("0123456789")
            .address(getTestAddress("applicant 2 address"))
            .designated(YesNo.NO.getValue())
            .colleagues(wrapElements(getTestColleague(2)))
            .build();
        Element<LocalAuthority> la2Element = element(la2);

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(la1Element, la2Element))
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .applicants(List.of(
                CafcassApiApplicant.builder()
                    .id(la1Element.getId().toString())
                    .name("Applicant 1 name")
                    .email("applicant1@test.com")
                    .phone("0123456789")
                    .address(getExpectedAddress("applicant 1 address"))
                    .designated(true)
                    .colleagues(List.of(getExpectedColleague(1)))
                    .build(),
                CafcassApiApplicant.builder()
                    .id(la2Element.getId().toString())
                    .name("Applicant 2 name")
                    .email("applicant2@test.com")
                    .phone("0123456789")
                    .address(getExpectedAddress("applicant 2 address"))
                    .designated(false)
                    .colleagues(List.of(getExpectedColleague(2)))
                    .build()))
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldBuildEmptyListIfNoApplicant() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().applicants(List.of()).build();

        testConvert(CaseData.builder().localAuthorities(null).build(), expected);
        testConvert(CaseData.builder().localAuthorities(List.of()).build(), expected);
    }
}
