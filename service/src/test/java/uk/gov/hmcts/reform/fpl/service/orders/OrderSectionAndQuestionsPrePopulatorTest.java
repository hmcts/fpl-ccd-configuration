package uk.gov.hmcts.reform.fpl.service.orders;

import com.google.common.collect.Maps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class OrderSectionAndQuestionsPrePopulatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final OrderSection ORDER_SECTION = OrderSection.SECTION_1;
    private static final OrderSection ANOTHER_ORDER_SECTION = OrderSection.SECTION_2;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final CaseDetails CASE_DETAILS = mock(CaseDetails.class);
    private static final Map<String, Object> PRE_POPULATE_SECTION_DATA = Maps.newHashMap(
        Map.of("sectionField", "sectionValue"));
    private static final Map<String, Object> PRE_POPULATE_WHICH_CHILDREN_DATA = Maps.newHashMap(
        Map.of("wcField", "wcFieldValue"));
    private static final Map<String, Object> PRE_POPULATE_SOME_DATA = Maps.newHashMap(
        Map.of("someField", "someFieldValue"));
    private static final Map<String, Object> PRE_POPULATE_SOME_OTHER_DATA = Maps.newHashMap(
        Map.of("someOtherField", "someOtherFieldValue"));

    private final OrderSectionAndQuestionsPrePopulatorHolder holder =
        mock(OrderSectionAndQuestionsPrePopulatorHolder.class);
    private final OrderSectionPrePopulator sectionPrePopulator = mock(OrderSectionPrePopulator.class);
    private final OrderSectionPrePopulator anotherSectionPrePopulator = mock(OrderSectionPrePopulator.class);
    private final QuestionBlockOrderPrePopulator whichChildreQuestionBlockPrePopulator = mock(
        QuestionBlockOrderPrePopulator.class);
    private final QuestionBlockOrderPrePopulator sectionQuestionBlockPopulator =
        mock(QuestionBlockOrderPrePopulator.class);
    private final QuestionBlockOrderPrePopulator anotherSectionQuestionBlockPopulator = mock(
        QuestionBlockOrderPrePopulator.class);

    private final OrderSectionAndQuestionsPrePopulator underTest = new OrderSectionAndQuestionsPrePopulator(
        holder
    );

    @BeforeEach
    void setUp() {
    }

    @Test
    void prePopulateWhenNoSectionNorQuestions() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of());
        when(holder.questionBlockToPopulator()).thenReturn(Map.of());

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            ORDER_SECTION,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());

    }

    @Test
    void prePopulateWhenNoSectionMatched() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of(ANOTHER_ORDER_SECTION,
            anotherSectionPrePopulator));
        when(holder.questionBlockToPopulator()).thenReturn(Map.of());

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            ORDER_SECTION,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());
        verifyNoInteractions(anotherSectionPrePopulator);

    }

    @Test
    void prePopulateWhenSectionMatched() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of(ORDER_SECTION,
            sectionPrePopulator));
        when(holder.questionBlockToPopulator()).thenReturn(Maps.newHashMap());

        when(sectionPrePopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(PRE_POPULATE_SECTION_DATA);

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            ORDER_SECTION,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(PRE_POPULATE_SECTION_DATA);

    }

    @Test
    void prePopulateWhenNoQuestionMatched() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of());
        when(holder.questionBlockToPopulator()).thenReturn(Map.of(
            OrderQuestionBlock.WHICH_CHILDREN, whichChildreQuestionBlockPrePopulator
        ));

        when(whichChildreQuestionBlockPrePopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(
            PRE_POPULATE_WHICH_CHILDREN_DATA);

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            ANOTHER_ORDER_SECTION,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());

    }

    @Test
    void prePopulateWhenQuestionMatched() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of());
        when(holder.questionBlockToPopulator()).thenReturn(Map.of(
            OrderQuestionBlock.WHICH_CHILDREN, whichChildreQuestionBlockPrePopulator
        ));

        when(whichChildreQuestionBlockPrePopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(
            PRE_POPULATE_WHICH_CHILDREN_DATA);

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            OrderSection.SECTION_3,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(PRE_POPULATE_WHICH_CHILDREN_DATA);

    }

    @Test
    void prePopulateWhenMultipleQuestionMatched() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of());
        when(holder.questionBlockToPopulator()).thenReturn(Map.of(
            OrderQuestionBlock.APPROVER, sectionQuestionBlockPopulator,
            OrderQuestionBlock.APPROVAL_DATE, anotherSectionQuestionBlockPopulator
        ));

        when(sectionQuestionBlockPopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(PRE_POPULATE_SOME_DATA);
        when(anotherSectionQuestionBlockPopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(
            PRE_POPULATE_SOME_OTHER_DATA);

        Map<String, Object> actual = underTest.prePopulate(ORDER,
            OrderSection.SECTION_2,
            CASE_DATA,
            CASE_DETAILS);

        assertThat(actual).isEqualTo(
            Map.of(
                "someField", "someFieldValue",
                "someOtherField", "someOtherFieldValue"
            )
        );

    }

    @Test
    void prePopulateWhenMultipleQuestionMatchedWithDuplication() {
        when(holder.sectionBlockToPopulator()).thenReturn(Map.of());
        when(holder.questionBlockToPopulator()).thenReturn(Map.of(
            OrderQuestionBlock.APPROVER, sectionQuestionBlockPopulator,
            OrderQuestionBlock.APPROVAL_DATE, anotherSectionQuestionBlockPopulator
        ));

        when(sectionQuestionBlockPopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(PRE_POPULATE_SOME_DATA);
        when(anotherSectionQuestionBlockPopulator.prePopulate(CASE_DATA, CASE_DETAILS)).thenReturn(
            PRE_POPULATE_SOME_DATA);

        Assertions.assertThatThrownBy(() -> underTest.prePopulate(ORDER,
            OrderSection.SECTION_2,
            CASE_DATA,
            CASE_DETAILS))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Duplicate key someField (attempted merging values someFieldValue and someFieldValue)");


    }
}
