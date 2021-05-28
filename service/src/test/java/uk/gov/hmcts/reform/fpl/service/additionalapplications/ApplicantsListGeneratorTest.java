package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ApplicantsListGeneratorTest {

    public static final RespondentParty RESPONDENT_PARTY_1 = RespondentParty.builder()
        .firstName("Joe").lastName("Blogs").build();

    public static final RespondentParty RESPONDENT_PARTY_2 = RespondentParty.builder()
        .firstName("David").lastName("Summer").build();

    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    private ApplicantsListGenerator underTest = new ApplicantsListGenerator(dynamicListService);

    @Test
    void shouldReturnAllApplicantsList() {
        List<Element<Respondent>> respondents = List.of(
            element(Respondent.builder().party(RESPONDENT_PARTY_1).build()),
            element(Respondent.builder().party(RESPONDENT_PARTY_2).build()));

        List<Element<Other>> others = List.of(
            element(Other.builder().name("Bob").build()),
            element(Other.builder().name("Smith").build()));

        CaseData caseData = CaseData.builder()
            .caseLocalAuthorityName("Swansea local authority")
            .respondents1(respondents)
            .others(Others.builder()
                .firstOther(Other.builder().name("Ross").build())
                .additionalOthers(others)
                .build())
            .build();

        DynamicList actualDynamicList = underTest.buildApplicantsList(caseData);

        Assertions.assertThat(actualDynamicList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Swansea local authority, Applicant",
                RESPONDENT_PARTY_1.getFullName() + ", Respondent 1",
                RESPONDENT_PARTY_2.getFullName() + ", Respondent 2",
                "Ross, Other to be given notice 1",
                "Bob, Other to be given notice 2",
                "Smith, Other to be given notice 3",
                "Someone else");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnSomeoneElseWhenLocalAuthorityNameAndRespondentsAndOthersDoNotExist(String caseLocalAuthority) {
        CaseData caseData = CaseData.builder().caseLocalAuthorityName(caseLocalAuthority).build();

        DynamicList actualDynamicList = underTest.buildApplicantsList(caseData);

        Assertions.assertThat(actualDynamicList).isEqualTo(DynamicList.builder()
            .value(DynamicListElement.builder().build())
            .listItems(List.of(DynamicListElement.builder().code("SOMEONE_ELSE").label("Someone else").build()))
            .build());
    }

}
