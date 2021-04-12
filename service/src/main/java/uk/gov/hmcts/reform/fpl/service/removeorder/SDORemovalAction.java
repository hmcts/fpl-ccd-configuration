package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SDORemovalAction implements OrderRemovalAction {
    private final IdentityService identityService;

    @Override
    public boolean isAccepted(RemovableOrder removableOrder) {
        return removableOrder instanceof StandardDirectionOrder;
    }

    @Override
    public void populateCaseFields(CaseData caseData, CaseDetailsMap data, UUID removedOrderId,
                                   RemovableOrder removableOrder) {
        StandardDirectionOrder standardDirectionOrder = (StandardDirectionOrder) removableOrder;

        data.put("orderToBeRemoved", standardDirectionOrder.getOrderDoc());
        data.put("orderTitleToBeRemoved", "Gatekeeping order");
        data.put("showRemoveSDOWarningFlag", YES.getValue());
        data.put("showRemoveCMOFieldsFlag", NO.getValue());
    }

    @Override
    public void remove(CaseData caseData, CaseDetailsMap data, UUID removedOrderId, RemovableOrder removableOrder) {
        StandardDirectionOrder standardDirectionOrder = (StandardDirectionOrder) removableOrder;

        standardDirectionOrder = standardDirectionOrder.toBuilder()
            .removalReason(caseData.getReasonToRemoveOrder())
            .judgeAndLegalAdvisor(null)
            .build();

        data.remove("standardDirectionOrder");
        data.remove("noticeOfProceedingsBundle");

        List<Element<StandardDirectionOrder>> hiddenSDOs = caseData.getHiddenStandardDirectionOrders();

        hiddenSDOs.add(element(identityService.generateId(), standardDirectionOrder));

        data.put("hiddenStandardDirectionOrders", hiddenSDOs);
        data.put("state", GATEKEEPING);
        clearDirectionsForSDO(data);
    }

    private void clearDirectionsForSDO(CaseDetailsMap data) {
        data.remove("allParties");
        data.remove("allPartiesCustom");
        data.remove("localAuthorityDirections");
        data.remove("localAuthorityDirectionsCustom");
        data.remove("courtDirections");
        data.remove("courtDirectionsCustom");
        data.remove("cafcassDirections");
        data.remove("cafcassDirectionsCustom");
        data.remove("otherPartiesDirections");
        data.remove("otherPartiesDirectionsCustom");
        data.remove("respondentDirections");
        data.remove("respondentDirectionsCustom");
    }
}
