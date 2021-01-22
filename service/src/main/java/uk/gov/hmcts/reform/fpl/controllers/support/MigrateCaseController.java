package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private final CMORemovalAction cmoRemovalAction;
    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2623".equals(migrationId)) {
            run2623(caseDetails);
        }

        if ("FPLA-2636".equals(migrationId)) {
            run2636(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2623(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50072".equals(caseData.getFamilyManCaseNumber())) {

            removeDuplicateOrder(caseData, caseDetails, 3);
        }
    }

    private void run2636(CaseDetails caseDetails) {
        if ("1605534056983302".equals(caseDetails.getId().toString())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeDuplicateOrder(CaseData caseData, CaseDetails data, int orderElement) {
        List<Element<GeneratedOrder>> orders = caseData.getOrderCollection();

        if (isEmpty(orders)) {
            data.getData().remove("orderCollection");
        } else {
            if (orders.size() > orderElement) {
                orders.remove(orderElement);
                data.getData().put("orderCollection", orders);
            }
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<CaseManagementOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
    }
}
