package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiOther;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public class CafcassApiOthersConverterTest extends CafcassApiConverterTestBase {
    CafcassApiOthersConverterTest() {
        super(new CafcassApiOthersConverter());
    }

    @Test
    void shouldConvertOthers() {
        Other firstOthers = Other.builder()
            .name("First Other")
            .dateOfBirth("2000-01-01")
            .gender(Gender.OTHER.getLabel())
            .genderIdentification("genderIdentification")
            .birthPlace("birthPlace")
            .addressKnow(YesNo.YES.getValue())
            .address(getTestAddress())
            .telephone(getTestTelephone().getTelephoneNumber())
            .litigationIssues("litigationIssues")
            .litigationIssuesDetails("litigationIssuesDetails")
            .detailsHidden(YesNo.YES.toString())
            .detailsHiddenReason("detailsHiddenReason")
            .build();

        Other secondOther = Other.builder()
            .name("Second Other")
            .dateOfBirth("2000-01-01")
            .gender(Gender.MALE.getLabel())
            .birthPlace("birthPlace")
            .addressKnow(YesNo.NO.getValue())
            .addressNotKnowReason("addressNotKnowReason")
            .detailsHidden(YesNo.NO.toString())
            .build();
        Element<Other> secondOtherElement = element(secondOther);

        Other emptyOther = Other.builder().build();
        Element<Other> emptyOtherElement = element(emptyOther);

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(firstOthers)
                .additionalOthers(List.of(secondOtherElement, emptyOtherElement))
                .build())
            .build();

        List<CafcassApiOther> expectedOthers = List.of(
            CafcassApiOther.builder()
                .name("First Other")
                .dateOfBirth("2000-01-01")
                .gender(Gender.OTHER.toString())
                .genderIdentification("genderIdentification")
                .birthPlace("birthPlace")
                .addressKnown(true)
                .address(getExpectedAddress())
                .telephone(getExpectedTelephone())
                .litigationIssues("litigationIssues")
                .litigationIssuesDetails("litigationIssuesDetails")
                .detailsHidden(true)
                .detailsHiddenReason("detailsHiddenReason")
                .build(),
            CafcassApiOther.builder()
                .name("Second Other")
                .dateOfBirth("2000-01-01")
                .gender(Gender.MALE.toString())
                .birthPlace("birthPlace")
                .addressKnown(false)
                .addressUnknownReason("addressNotKnowReason")
                .detailsHidden(false)
                .build(),
            CafcassApiOther.builder().build()
        );

        testConvert(caseData, CafcassApiCaseData.builder().others(expectedOthers).build());
    }

    @Test
    void shouldReturnEmptyListIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().others(List.of()).build();

        testConvert(CaseData.builder().others(null).build(), expected);
        testConvert(CaseData.builder().others(Others.builder().build()).build(), expected);
    }
}
