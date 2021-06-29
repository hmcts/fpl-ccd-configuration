package uk.gov.hmcts.reform.fpl.service.orders.history;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SealedOrderHistoryExtraTitleGenerator {

    public String generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();

        switch (manageOrdersEventData.getManageOrdersType()) {
            case OTHER_ORDER:
                return manageOrdersEventData.getManageOrdersUploadTypeOtherTitle();
            case C21_BLANK_ORDER:
                return manageOrdersEventData.getManageOrdersTitle();
            default:
                return null;
        }
    }
}
