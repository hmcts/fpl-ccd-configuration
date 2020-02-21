package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Service
public class OrderValidationService {
    public List<String> validate(final CaseData caseData) {
        List<String> validationErrors = new ArrayList<>();
        if (SEALED == caseData.getStandardDirectionOrder().getOrderStatus()
            && isEmpty(caseData.getHearingDetails())) {
            validationErrors.add("You need to enter a hearing date.");
        }

        if (SEALED == caseData.getStandardDirectionOrder().getOrderStatus()
            && isNull(caseData.getAllocatedJudge())) {
            validationErrors.add("You need to enter the allocated judge.");
        }

        return validationErrors;
    }
}
