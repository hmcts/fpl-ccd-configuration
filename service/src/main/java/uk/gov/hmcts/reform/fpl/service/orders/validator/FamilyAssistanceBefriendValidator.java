package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FAMILY_ASSISTANCE_ORDER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FamilyAssistanceBefriendValidator implements QuestionBlockOrderValidator {

    private static final String MESSAGE = "You cannot name the same party to be befriended more than once.";

    @Override
    public OrderQuestionBlock accept() {
        return FAMILY_ASSISTANCE_ORDER;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        UUID person1 = caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended1().getValueCodeAsUUID();
        UUID person2 = caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended2().getValueCodeAsUUID();
        UUID person3 = caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended3().getValueCodeAsUUID();

        if (person1.equals(person2) || person1.equals(person3) || person2.equals(person3)) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
