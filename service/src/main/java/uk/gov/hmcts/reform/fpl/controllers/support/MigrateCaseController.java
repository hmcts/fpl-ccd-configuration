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
import uk.gov.hmcts.reform.fpl.service.removeorder.GeneratedOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private final CMORemovalAction cmoRemovalAction;
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;
    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2480".equals(migrationId)) {
            run2480(caseDetails);
        }

        if ("FPLA-2608".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2608(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2480(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        if ("LE20C50003".equals(caseData.getFamilyManCaseNumber())) {
            removeDraftCaseManagementOrder(caseDetails, 0);
        }
    }

    private void run2608(CaseDetails caseDetails) {
        if ("1595320156232721".equals(caseDetails.getId().toString())) {
            CaseData caseData = getCaseData(caseDetails);
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (caseData.getOrderCollection().size() < 7) {
                throw new IllegalArgumentException(String.format("Expected to have at least 8 generated orders"
                        + " but found %s", caseData.getOrderCollection().size()));
            }

            UUID orderSixId = UUID.fromString("aaa8e7b5-824c-4fde-9ab6-f0abf28a22be");
            UUID orderSevenId = UUID.fromString("fa2d3751-3517-455d-a67b-35232d257665");

            Optional<Element<GeneratedOrder>> generatedOrderSix
                = findElement(orderSixId, caseData.getOrderCollection());

            Optional<Element<GeneratedOrder>> generatedOrderSeven
                = findElement(orderSevenId, caseData.getOrderCollection());

            if (generatedOrderSix.isEmpty()) {
                throw new IllegalArgumentException(String.format("Could not find generated order %s",
                    orderSixId));
            }

            if (generatedOrderSeven.isEmpty()) {
                throw new IllegalArgumentException(String.format("Could not find generated order %s",
                    orderSevenId));
            }

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, generatedOrderSeven.get().getId(),
                generatedOrderSeven.get().getValue());

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, generatedOrderSix.get().getId(),
                generatedOrderSix.get().getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }

    private void removeDraftCaseManagementOrder(CaseDetails caseDetails, int index) {
        CaseData caseData = getCaseData(caseDetails);
        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }
        Element<CaseManagementOrder> draftCmo = caseData.getDraftUploadedCMOs().get(index);
        cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, draftCmo);
    }
}
