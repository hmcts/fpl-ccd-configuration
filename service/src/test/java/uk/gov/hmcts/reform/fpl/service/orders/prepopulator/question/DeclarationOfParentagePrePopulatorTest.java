package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
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

    @Test
    @SuppressWarnings("unchecked")
    void prePopulate() {
        List<Element<Respondent>> respondents = List.of(
            element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                Respondent.builder().party(RespondentParty.builder().firstName("Peter").lastName("Smith").build()
                ).build()));

        Others others = Others.builder()
            .firstOther(Other.builder().name("Jack Johnson").build())
            .additionalOthers(wrapElements(Other.builder().name("Alan Smith").build()))
            .build();
        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                LocalAuthority.builder().name("Swansea local authority").build())))
            .respondents1(respondents)
            .others(others)
            .build();

        when(dynamicListService.asDynamicList(isA(Map.class))).thenAnswer((a) -> DynamicList.builder()
            .listItems(new ArrayList<DynamicListElement>())
            .build());

        DynamicList applicants = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                    .code("11111111-1111-1111-1111-111111111111")
                    .label("Swansea local authority").build()))
            .build();
        DynamicList hearingParties = DynamicList.builder().listItems(
                List.of(DynamicListElement.builder()
                    .code("Swansea local authority")
                    .label("Swansea local authority, Applicant").build()))
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
                "manageOrdersParentageAction", DynamicList.builder().listItems(
                    List.of(DynamicListElement.builder().code("IS").label("is").build(),
                        DynamicListElement.builder().code("ISNOT").label("is not").build(),
                        DynamicListElement.builder().code("WAS").label("was").build(),
                        DynamicListElement.builder().code("WASNOT").label("was not").build()
                    )
                ).build()
            )
        );
    }
}
