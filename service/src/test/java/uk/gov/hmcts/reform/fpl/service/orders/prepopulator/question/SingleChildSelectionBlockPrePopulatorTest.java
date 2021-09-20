package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.assertions.DynamicListAssert;

import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SELECT_SINGLE_CHILD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class SingleChildSelectionBlockPrePopulatorTest {

    private static final Element<Child> FIRST_CHILD_DETAILS = element(
        UUID.randomUUID(),
        Child.builder().party(ChildParty.builder().firstName("Johnny").lastName("Smith").build()).build()
    );
    private static final Element<Child> SECOND_CHILD_DETAILS = element(
        UUID.randomUUID(),
        Child.builder().party(ChildParty.builder().firstName("Ted").lastName("Baker").build()).build()
    );

    private final SingleChildSelectionBlockPrePopulator underTest = new SingleChildSelectionBlockPrePopulator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(SELECT_SINGLE_CHILD);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder()
            .children1(asList(FIRST_CHILD_DETAILS, SECOND_CHILD_DETAILS))
            .build();

        Map<String, Object> prePopulatedData = underTest.prePopulate(caseData);

        assertThat(prePopulatedData)
            .extractingByKey("whichChildIsTheOrderFor")
            .asInstanceOf(DynamicListAssert.getInstanceOfAssertFactory())
            .hasElement(FIRST_CHILD_DETAILS.getId(), "Johnny Smith")
            .hasElement(SECOND_CHILD_DETAILS.getId(), "Ted Baker");
    }

}
