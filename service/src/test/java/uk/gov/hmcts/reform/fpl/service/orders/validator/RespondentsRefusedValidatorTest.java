package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.RESPONDENTS_REFUSED;

class RespondentsRefusedValidatorTest {

    private static final String MESSAGE = "Select the appointed person(s) being refused contact";

    private final RespondentsRefusedValidator underTest = new RespondentsRefusedValidator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(RESPONDENTS_REFUSED);
    }

    @Test
    void validateAnyRespondentsSelected() {
        CaseData caseData = CaseData.builder()
            .orderAppliesToAllChildren("No")
            .respondentsRefusedSelector(Selector.builder().selected(List.of(1, 2)).build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void validateNoRespondentsSelected() {
        CaseData caseData = CaseData.builder()
            .respondentsRefusedSelector(Selector.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
