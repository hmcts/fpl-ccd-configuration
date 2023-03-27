package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.others.OthersListGenerator;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AllowedContactPrePopulator implements QuestionBlockOrderPrePopulator {

    private final OthersListGenerator othersListGenerator;

    private final DynamicListService dynamicListService;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.PARTY_ALLOWED_CONTACTS_AND_CONDITIONS;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        DynamicList allowedContacts = dynamicListService.asDynamicList(Map.of());
        // build respondent
        allowedContacts.getListItems().add(DynamicListElement.builder()
            .code("").label("-- Respondent --").build());
        caseData.getRespondents1().forEach(r ->
            allowedContacts.getListItems().add(DynamicListElement.builder()
                .code(r.getId())
                .label(r.getValue().getParty().getFullName())
                .build()));
        if (!caseData.getAllOthers().isEmpty()) {
            allowedContacts.getListItems().add(DynamicListElement.builder()
                .code("").label("-- Others to be given notice --").build());
            DynamicList otherList = othersListGenerator.buildOthersList(caseData.getAllOthers());
            allowedContacts.getListItems().addAll(otherList.getListItems());
        }

        return Map.of(
            "manageOrdersAllowedContact1", allowedContacts,
            "manageOrdersAllowedContact2", allowedContacts,
            "manageOrdersAllowedContact3", allowedContacts
        );
    }
}
