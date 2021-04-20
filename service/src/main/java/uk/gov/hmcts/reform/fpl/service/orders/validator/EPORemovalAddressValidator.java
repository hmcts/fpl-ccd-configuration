package uk.gov.hmcts.reform.fpl.service.orders.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_REMOVAL_ADDRESS;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EPORemovalAddressValidator implements QuestionBlockOrderValidator {

    private static final String INVALID_ADDRESS_LINE_MESSAGE = "Enter a valid address for the contact";
    private static final String INVALID_POST_CODE_MESSAGE = "Enter a postcode for the contact";

    @Override
    public OrderQuestionBlock accept() {
        return EPO_REMOVAL_ADDRESS;
    }

    @Override
    public List<String> validate(CaseData caseData) {
        final ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        final Address removalAddress = eventData.getManageOrdersEpoRemovalAddress();

        List<String> errors = new ArrayList<>();

        if (eventData.getManageOrdersEpoType() == EPOType.REMOVE_TO_ACCOMMODATION) {
            return List.of();
        }

        if (isEmpty(removalAddress.getAddressLine1())) {
            errors.add(INVALID_ADDRESS_LINE_MESSAGE);
        }

        if (isEmpty(removalAddress.getPostcode())) {
            errors.add(INVALID_POST_CODE_MESSAGE);
        }

        return errors;
    }

}
