package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRespondent;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiRespondentsConverterTest extends CafcassApiConverterTestBase {
    CafcassApiRespondentsConverterTest() {
        super(new CafcassApiRespondentsConverter());
    }

    @Test
    void shouldConvertAllRespondents() {
        Respondent respondentWithAddressHidden = Respondent.builder()
            .solicitor(getTestRespondentSolicitor())
            .party(RespondentParty.builder()
                .firstName("Respondent1")
                .lastName("LastName")
                .gender(Gender.MALE.toString())
                .addressKnow(YesNo.YES.toString())
                .address(getTestAddress())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .telephoneNumber(getTestTelephone())
                .litigationIssues("litigationIssues")
                .litigationIssuesDetails("litigationIssuesDetails")
                .contactDetailsHidden(YesNo.YES.toString())
                .contactDetailsHiddenReason("contactDetailsHiddenReason")
                .relationshipToChild("relationshipToChild")
                .build())
            .build();

        Respondent respondentWithMandatoryFieldOnly = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Respondent2")
                .lastName("LastName")
                .gender(Gender.OTHER.toString())
                .genderIdentification("genderIdentification")
                .addressKnow(YesNo.NO.toString())
                .addressNotKnowReason("addressUnknownReason")
                .relationshipToChild("relationshipToChild")
                .build())
            .build();

        Respondent emptyRespondent = Respondent.builder().build();

        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondentWithAddressHidden, respondentWithMandatoryFieldOnly, emptyRespondent))
            .build();

        List<CafcassApiRespondent> expectedList = List.of(
            CafcassApiRespondent.builder()
                .firstName("Respondent1")
                .lastName("LastName")
                .gender(Gender.MALE.toString())
                .addressKnown(true)
                .address(getExpectedAddress())
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .telephoneNumber(getExpectedTelephone())
                .litigationIssues("litigationIssues")
                .litigationIssuesDetails("litigationIssuesDetails")
                .contactDetailsHidden(true)
                .contactDetailsHiddenReason("contactDetailsHiddenReason")
                .relationshipToChild("relationshipToChild")
                .solicitor(getExpectedSolicitor())
                .build(),
            CafcassApiRespondent.builder()
                .firstName("Respondent2")
                .lastName("LastName")
                .gender(Gender.OTHER.toString())
                .genderIdentification("genderIdentification")
                .addressKnown(false)
                .addressUnknownReason("addressUnknownReason")
                .relationshipToChild("relationshipToChild")
                .build(),
            CafcassApiRespondent.builder().build()
        );

        testConvert(caseData, CafcassApiCaseData.builder().respondents(expectedList).build());
    }

    @Test
    void shouldReturnEmptyListIfNullOrEmpty() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().respondents(List.of()).build();

        testConvert(CaseData.builder().respondents1(null).build(), expected);
        testConvert(CaseData.builder().respondents1(List.of()).build(), expected);
    }
}
