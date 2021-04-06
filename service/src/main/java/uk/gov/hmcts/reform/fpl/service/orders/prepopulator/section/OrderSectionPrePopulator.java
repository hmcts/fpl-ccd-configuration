package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

public interface OrderSectionPrePopulator {

    OrderSection accept();

    Map<String,Object> prePopulate(CaseData caseData, CaseDetails caseDetails);
}
