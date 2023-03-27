package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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
        List<UUID> uuids = new ArrayList<>();

        if (isNotEmpty(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended1())) {
            uuids.add(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended1().getValueCodeAsUUID());
        }
        if (isNotEmpty(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended2())) {
            uuids.add(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended2().getValueCodeAsUUID());
        }
        if (isNotEmpty(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended3())) {
            uuids.add(caseData.getManageOrdersEventData().getManageOrdersPartyToBeBefriended3().getValueCodeAsUUID());
        }

        Set<UUID> uniques = new HashSet<>(uuids);

        // we have duplicates
        if (uuids.size() != uniques.size()) {
            return List.of(MESSAGE);
        }

        return List.of();
    }
}
