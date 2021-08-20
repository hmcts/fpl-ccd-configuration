package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.model.order.Order.AMENED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.CREATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.UPLOAD;

@Component
public class ManageOrdersCaseDataFixer {

    // this is to workaround an EXUI bug that don't retain hidden fields
    public CaseData fix(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        OrderOperation operation = eventData.getManageOrdersOperation();
        OrderOperation closedOperation = eventData.getManageOrdersOperationClosedState();

        if (CREATE == closedOperation && CLOSED == caseData.getState()) {
            return caseData.toBuilder().manageOrdersEventData(
                eventData.toBuilder()
                    .manageOrdersType(C21_BLANK_ORDER)
                    .manageOrdersOperation(CREATE)
                    .build()
            ).build();
        }

        if (UPLOAD == operation) {
            return caseData.toBuilder().manageOrdersEventData(
                eventData.toBuilder()
                    .manageOrdersType(eventData.getManageOrdersUploadType())
                    .build()
            ).build();
        }

        if (AMEND == operation || AMEND == closedOperation) {
            return caseData.toBuilder().manageOrdersEventData(
                eventData.toBuilder()
                    .manageOrdersType(AMENED_ORDER)
                    .build()
            ).build();
        }

        return caseData;
    }

    public CaseDetails fixAndRetriveCaseDetails(CaseDetails caseDetails) {
        if (!isNull((caseDetails.getData().get("manageOrdersOperation")))) {
            String operation = caseDetails.getData().get("manageOrdersOperation").toString();

            if (!AMEND.toString().equals(operation)) {
                caseDetails.getData().remove("manageOrdersAmendmentList");
            }
        }

        return caseDetails;
    }
}
