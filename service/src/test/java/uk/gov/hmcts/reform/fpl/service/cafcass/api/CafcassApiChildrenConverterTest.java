package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiChild;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiChildrenConverterTest extends CafcassApiConverterTestBase {

    CafcassApiChildrenConverterTest() {
        super(new CafcassApiChildrenConverter());
    }

    @Test
    void shouldConvertAllChildren() {
        Child childWithAllFields = Child.builder()
            .party(ChildParty.builder()
                .firstName("Child")
                .lastName("One")
                .dateOfBirth(LocalDate.of(2024, 1, 1))
                .gender(ChildGender.BOY)
                .genderIdentification("genderIdentification")
                .livingSituation("livingSituation")
                .livingSituationDetails("livingSituationDetails")
                .address(getTestAddress())
                .careAndContactPlan("careAndContactPlan")
                .detailsHidden(YesNo.YES.toString())
                .detailsHiddenReason("detailsHiddenReason")
                .socialWorkerName("socialWorkerName")
                .socialWorkerTelephoneNumber(getTestTelephone())
                .additionalNeeds(YesNo.YES.toString())
                .additionalNeedsDetails("additionalNeedsDetails")
                .litigationIssues("litigationIssues")
                .litigationIssuesDetails("litigationIssuesDetails")
                .fathersResponsibility("fathersResponsibility")
                .build())
            .solicitor(getTestRespondentSolicitor())
            .build();

        Child childWithMandatoryFieldOnly = Child.builder()
            .party(ChildParty.builder()
                .firstName("Child")
                .lastName("Two")
                .dateOfBirth(LocalDate.of(2023, 1, 1))
                .gender(ChildGender.GIRL)
                .build())
            .build();

        Child emptyChild = Child.builder().build();

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(childWithAllFields, childWithMandatoryFieldOnly, emptyChild))
            .build();

        testConvert(caseData, CafcassApiCaseData.builder()
            .children(List.of(
                CafcassApiChild.builder()
                    .firstName("Child")
                    .lastName("One")
                    .dateOfBirth(LocalDate.of(2024, 1, 1))
                    .gender(Gender.MALE.toString())
                    .genderIdentification("genderIdentification")
                    .livingSituation("livingSituation")
                    .livingSituationDetails("livingSituationDetails")
                    .address(getExpectedAddress())
                    .careAndContactPlan("careAndContactPlan")
                    .detailsHidden(true)
                    .socialWorkerName("socialWorkerName")
                    .socialWorkerTelephoneNumber(getExpectedTelephone())
                    .additionalNeeds(true)
                    .additionalNeedsDetails("additionalNeedsDetails")
                    .litigationIssues("litigationIssues")
                    .litigationIssuesDetails("litigationIssuesDetails")
                    .fathersResponsibility("fathersResponsibility")
                    .solicitor(getExpectedSolicitor())
                    .build(),
                CafcassApiChild.builder()
                    .firstName("Child")
                    .lastName("Two")
                    .dateOfBirth(LocalDate.of(2023, 1, 1))
                    .gender(Gender.FEMALE.toString())
                    .build(),
                CafcassApiChild.builder().build()
            )).build());
    }


    @Test
    void shouldReturnEmptyLis() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().children(List.of()).build();

        testConvert(CaseData.builder().children1(null).build(), expected);
        testConvert(CaseData.builder().children1(List.of()).build(), expected);
    }
}
