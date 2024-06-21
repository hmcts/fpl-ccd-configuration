package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.AmendableOrder;

import java.util.List;

import static java.util.Collections.unmodifiableList;

@Component
public class AmendableGeneratedOrderProvider implements AmendableListItemProvider {
    @Override
    public List<Element<? extends AmendableOrder>> provideListItems(CaseData caseData) {
        return unmodifiableList(caseData.getAllOrderCollections());
    }
}
