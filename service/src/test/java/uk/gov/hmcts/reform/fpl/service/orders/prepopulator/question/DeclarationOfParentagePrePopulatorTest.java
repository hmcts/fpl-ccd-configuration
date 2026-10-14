package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class DeclarationOfParentagePrePopulatorTest {

    private final DynamicListService dynamicListService = mock(DynamicListService.class);

    private final DeclarationOfParentagePrePopulator underTest
        = new DeclarationOfParentagePrePopulator(dynamicListService);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.DECLARATION_OF_PARENTAGE);
    }

    private static Respondent createRespondent(String firstName, String lastName) {
        return createRespondent(firstName, lastName, null, null);
    }

    private static Respondent createRespondent(String firstName, String lastName,
                                               String solicitorFirstName, String solicitorLastName) {
        Respondent.RespondentBuilder builder = Respondent.builder()
            .party(RespondentParty.builder().firstName(firstName).lastName(lastName).build());
        if (solicitorFirstName != null && solicitorLastName != null) {
            builder.solicitor(RespondentSolicitor.builder()
                .firstName(solicitorFirstName)
                .lastName(solicitorLastName).build());
        }
        return builder.build();
    }

    private static Child createChild(String firstName, String lastName,
                                     String solicitorFirstName, String solicitorLastName) {
        Child.ChildBuilder builder = Child.builder()
            .party(ChildParty.builder().firstName(firstName).lastName(lastName).build());
        if (solicitorFirstName != null && solicitorLastName != null) {
            builder.solicitor(RespondentSolicitor.builder()
                .firstName(solicitorFirstName)
                .lastName(solicitorLastName).build());
        }
        return builder.build();
    }

    private static DynamicList getExpectedManageOrdersParentageAction() {
        return DynamicList.builder().listItems(
            List.of(DynamicListElement.builder().code("IS").label("is").build(),
                DynamicListElement.builder().code("ISNOT").label("is not").build(),
                DynamicListElement.builder().code("WAS").label("was").build(),
                DynamicListElement.builder().code("WASNOT").label("was not").build()
            )
        ).build();
    }

    private static List<Element<Other>> createOthers(String... names) {
        if (names == null) {
            return null;
        }

        List<Other> others = new ArrayList<>();
        for (String name : names) {
            others.add(Other.builder().firstName(name).build());
        }

        return wrapElements(others);
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulate() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("21111111-1111-1111-1111-111111111111"),
                createRespondent("Peter", "Smith")));

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .respondents1(respondents)
            .othersV2(createOthers("Jack Johnson", "Alan Smith"))
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("11111111-1111-1111-1111-111111111111")
                        .label("Swansea local authority").build(),
                    DynamicListElement.builder()
                        .code("21111111-1111-1111-1111-111111111111")
                        .label("Peter Smith").build())
            )
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
                List.of(
                    DynamicListElement.builder()
                        .code("Swansea local authority")
                        .label("Swansea local authority, Applicant").build(),
                    DynamicListElement.builder()
                        .code("Peter Smith")
                        .label("Peter Smith, Respondent 1").build())
            )
            .build();
        DynamicList personWhoseParenthoodIs = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("Peter Smith")
                        .label("Peter Smith, Respondent 1").build(),
                    DynamicListElement.builder()
                        .code("Jack Johnson")
                        .label("Jack Johnson, Other to be given notice 1").build(),
                    DynamicListElement.builder()
                        .code("Alan Smith")
                        .label("Alan Smith, Other to be given notice 2").build()))
            .build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersParentageApplicant", applicants,
                "manageOrdersHearingParty1", hearingParties,
                "manageOrdersHearingParty2", hearingParties,
                "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
                "manageOrdersParentageAction", getExpectedManageOrdersParentageAction()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulateWithRespondentSolicitor() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("21111111-1111-1111-1111-111111111111"),
                createRespondent("Peter", "Smith", "Peter", "Solicitor")));

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .respondents1(respondents)
            .othersV2(createOthers("Jack Johnson", "Alan Smith"))
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("11111111-1111-1111-1111-111111111111")
                        .label("Swansea local authority").build(),
                    DynamicListElement.builder()
                        .code("21111111-1111-1111-1111-111111111111")
                        .label("Peter Smith").build()
                ))
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
            List.of(
                DynamicListElement.builder()
                    .code("Swansea local authority")
                    .label("Swansea local authority, Applicant").build(),
                DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build(),
                DynamicListElement.builder()
                    .code("Peter Solicitor")
                    .label("Peter Solicitor, Respondent 1's solicitor").build())
        ).build();
        DynamicList personWhoseParenthoodIs = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("Peter Smith")
                        .label("Peter Smith, Respondent 1").build(),
                    DynamicListElement.builder()
                        .code("Jack Johnson")
                        .label("Jack Johnson, Other to be given notice 1").build(),
                    DynamicListElement.builder()
                        .code("Alan Smith")
                        .label("Alan Smith, Other to be given notice 2").build()))
            .build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersParentageApplicant", applicants,
                "manageOrdersHearingParty1", hearingParties,
                "manageOrdersHearingParty2", hearingParties,
                "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
                "manageOrdersParentageAction", getExpectedManageOrdersParentageAction()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulateWithOnlyOneOtherPerson() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("21111111-1111-1111-1111-111111111111"),
                createRespondent("Peter", "Smith", "Peter", "Solicitor")));

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .respondents1(respondents)
            .othersV2(createOthers("Jack Johnson"))
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(
                    DynamicListElement.builder()
                        .code("11111111-1111-1111-1111-111111111111")
                        .label("Swansea local authority").build(),
                    DynamicListElement.builder()
                        .code("21111111-1111-1111-1111-111111111111")
                        .label("Peter Smith").build()
                )
            )
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
            List.of(
                DynamicListElement.builder()
                    .code("Swansea local authority")
                    .label("Swansea local authority, Applicant").build(),
                DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build(),
                DynamicListElement.builder()
                    .code("Peter Solicitor")
                    .label("Peter Solicitor, Respondent 1's solicitor").build())
        ).build();
        DynamicList personWhoseParenthoodIs = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("Peter Smith")
                        .label("Peter Smith, Respondent 1").build(),
                    DynamicListElement.builder()
                        .code("Jack Johnson")
                        .label("Jack Johnson, Other to be given notice 1").build()
                ))
            .build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersParentageApplicant", applicants,
                "manageOrdersHearingParty1", hearingParties,
                "manageOrdersHearingParty2", hearingParties,
                "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
                "manageOrdersParentageAction", getExpectedManageOrdersParentageAction()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulateWithoutOtherPerson() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("21111111-1111-1111-1111-111111111111"),
                createRespondent("Peter", "Smith", "Peter", "Solicitor")));

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .respondents1(respondents)
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("11111111-1111-1111-1111-111111111111")
                        .label("Swansea local authority").build(),
                    DynamicListElement.builder()
                        .code("21111111-1111-1111-1111-111111111111")
                        .label("Peter Smith").build()
                ))
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
            List.of(
                DynamicListElement.builder()
                    .code("Swansea local authority")
                    .label("Swansea local authority, Applicant").build(),
                DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build(),
                DynamicListElement.builder()
                    .code("Peter Solicitor")
                    .label("Peter Solicitor, Respondent 1's solicitor").build())
        ).build();
        DynamicList personWhoseParenthoodIs = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build()
                ))
            .build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersParentageApplicant", applicants,
                "manageOrdersHearingParty1", hearingParties,
                "manageOrdersHearingParty2", hearingParties,
                "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
                "manageOrdersParentageAction", getExpectedManageOrdersParentageAction()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void prePopulateWithChildSolicitor() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("21111111-1111-1111-1111-111111111111"),
                createRespondent("Peter", "Smith", "Peter", "Solicitor")));

        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .children1(wrapElements(createChild("Harley", "Queen", "Harley", "Solicitor")))
            .respondents1(respondents)
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                        .code("11111111-1111-1111-1111-111111111111")
                        .label("Swansea local authority").build(),
                    DynamicListElement.builder()
                        .code("21111111-1111-1111-1111-111111111111")
                        .label("Peter Smith").build()
                ))
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
            List.of(
                DynamicListElement.builder()
                    .code("Swansea local authority")
                    .label("Swansea local authority, Applicant").build(),
                DynamicListElement.builder()
                    .code("Harley Solicitor")
                    .label("Harley Solicitor, Child 1's solicitor").build(),
                DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build(),
                DynamicListElement.builder()
                    .code("Peter Solicitor")
                    .label("Peter Solicitor, Respondent 1's solicitor").build())
        ).build();
        DynamicList personWhoseParenthoodIs = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                    .code("Peter Smith")
                    .label("Peter Smith, Respondent 1").build()
                ))
            .build();

        Map<String, Object> actual = underTest.prePopulate(caseData);
        assertThat(actual).isEqualTo(
            Map.of(
                "manageOrdersParentageApplicant", applicants,
                "manageOrdersHearingParty1", hearingParties,
                "manageOrdersHearingParty2", hearingParties,
                "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
                "manageOrdersParentageAction", getExpectedManageOrdersParentageAction()
            )
        );
    }

}
