package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class FamilyAssistancePrePopulatorTest {

    private static final Element<Child> CHILD_ELEMENT = element(UUID.randomUUID(),
        Child.builder().party(ChildParty.builder().firstName("Jack").lastName("Smith").build()).build());
    private static final Element<Respondent> RESPONDENT_ELEMENT = element(UUID.randomUUID(),
        Respondent.builder().party(RespondentParty.builder().firstName("John").lastName("Smith").build()).build());
    private static final List<Element<String>> DYNAMIC_LIST_ELEMENTS = List.of(
        element(RESPONDENT_ELEMENT.getId(), "John Smith"),
        element(CHILD_ELEMENT.getId(), "Jack Smith")
    );

    private final FamilyAssistancePrePopulator underTest = new FamilyAssistancePrePopulator();

    @Test
    void shouldAcceptCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.FAMILY_ASSISTANCE_ORDER);
    }

    @Test
    void shouldPrePopulateThreeDynamicLists() {
        CaseData caseData = CaseData.builder()
            .children1(List.of(CHILD_ELEMENT))
            .respondents1(List.of(RESPONDENT_ELEMENT))
            .build();

        Map<String, Object> fields = underTest.prePopulate(caseData);
        DynamicList dynamicList = asDynamicList(DYNAMIC_LIST_ELEMENTS, label -> label);

        assertThat(fields).extracting("manageOrdersPartyToBeBefriended1",
            "manageOrdersPartyToBeBefriended2", "manageOrdersPartyToBeBefriended3")
            .containsExactly(dynamicList, dynamicList, dynamicList);
    }
}
