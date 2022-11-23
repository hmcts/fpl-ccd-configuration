package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor
public class FamilyAssistancePrePopulator implements QuestionBlockOrderPrePopulator {


    @Override
    public OrderQuestionBlock accept() {
        return FAMILY_ASSISTANCE_ORDER;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        List<Element<String>> parties = caseData.getAllRespondents().stream()
            .map(el -> element(el.getId(), el.getValue().getParty().getFullName()))
            .collect(Collectors.toList());
        parties.addAll(caseData.getAllChildren().stream()
            .map(el -> element(el.getId(), el.getValue().getParty().getFullName()))
            .collect(Collectors.toList()));

        return Map.of("manageOrdersPartyToBeBefriended1", asDynamicList(parties, label -> label),
            "manageOrdersPartyToBeBefriended2", asDynamicList(parties, label -> label),
            "manageOrdersPartyToBeBefriended3", asDynamicList(parties, label -> label)
        );
    }

}
