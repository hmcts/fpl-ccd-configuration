package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;

@Service
public class OrderValidationService {
    public List<String> validate(final CaseData caseData) {
        if (SEALED == caseData.getStandardDirectionOrder().getOrderStatus()
            && isEmpty(caseData.getHearingDetails())) {
            return singletonList(
                "This standard directions order does not have a hearing associated with it. "
                    + "Please enter a hearing date and resubmit the SDO");
        }

        return emptyList();
    }
}
