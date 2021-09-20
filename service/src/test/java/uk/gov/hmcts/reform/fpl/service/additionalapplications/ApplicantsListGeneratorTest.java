package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.ApplicantType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderApplicant;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ApplicantsListGeneratorTest {

    private static final RespondentParty RESPONDENT_PARTY_1 = RespondentParty.builder()
        .firstName("Joe").lastName("Blogs").build();

    private static final RespondentParty RESPONDENT_PARTY_2 = RespondentParty.builder()
        .firstName("David").lastName("Summer").build();

    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    private ApplicantsListGenerator underTest = new ApplicantsListGenerator(dynamicListService);

    private static CaseData caseData;

    @BeforeEach
    void setup() {
        List<Element<Respondent>> respondents = List.of(
            element(Respondent.builder().party(RESPONDENT_PARTY_1).build()),
            element(Respondent.builder().party(RESPONDENT_PARTY_2).build()));

        List<Element<Other>> others = List.of(
            element(Other.builder().name("Bob").build()),
            element(Other.builder().name("Smith").build()));

        caseData = CaseData.builder()
            .caseLocalAuthorityName("Swansea local authority")
            .respondents1(respondents)
            .others(Others.builder()
                .firstOther(Other.builder().name("Ross").build())
                .additionalOthers(others)
                .build())
            .build();
    }

    @Test
    void shouldReturnAllApplicantsList() {
        DynamicList actualDynamicList = underTest.buildApplicantsList(caseData);

        assertThat(actualDynamicList.getListItems())
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
        CaseData caseDataWithLA = CaseData.builder().caseLocalAuthorityName(caseLocalAuthority).build();

        DynamicList actualDynamicList = underTest.buildApplicantsList(caseDataWithLA);

        assertThat(actualDynamicList).isEqualTo(DynamicList.builder()
            .value(DynamicListElement.builder().build())
            .listItems(List.of(DynamicListElement.builder().code("SOMEONE_ELSE").label("Someone else").build()))
            .build());
    }

    @ParameterizedTest
    @MethodSource("additionalApplicationBundlesData")
    void shouldReturnApplicantNameAndType(AdditionalApplicationsBundle bundle, String name, ApplicantType type) {
        assertThat(underTest.getApplicant(caseData, bundle))
            .isEqualTo(OrderApplicant.builder().name(name).type(type).build());
    }

    private static Stream<Arguments> additionalApplicationBundlesData() {
        return Stream.of(
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .applicantName("Swansea local authority, Applicant").build()).build(),
                "Swansea local authority", ApplicantType.LOCAL_AUTHORITY),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .applicantName(RESPONDENT_PARTY_1.getFullName() + ", Respondent 1").build()).build(),
                RESPONDENT_PARTY_1.getFullName(), ApplicantType.RESPONDENT),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .applicantName("Smith, Other to be given notice 3").build()).build(),
                "Smith", ApplicantType.OTHER),
            Arguments.of(AdditionalApplicationsBundle.builder()
                    .otherApplicationsBundle(OtherApplicationsBundle.builder()
                        .applicantName("John White").build()).build(),
                "John White", ApplicantType.OTHER));
    }

}
