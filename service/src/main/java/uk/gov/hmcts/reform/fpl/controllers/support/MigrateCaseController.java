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
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.GeneratedOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.service.removeorder.SealedCMORemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;

    private final SealedCMORemovalAction sealedCMORemovalAction;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2693".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2693(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        if ("FPLA-2702".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2702(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        if ("FPLA-2724".equals(migrationId)) {
            run2724(caseDetails);
        }

        if ("FPLA-2706".equals(migrationId)) {
            run2706(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2706(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50049".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2702(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF21C50013".equals(caseData.getFamilyManCaseNumber())) {
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (isEmpty(caseData.getOrderCollection())) {
                throw new IllegalArgumentException("Case CF21C50013 does not contain generated orders");
            }

            Element<GeneratedOrder> orderOne = caseData.getOrderCollection().get(0);

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, orderOne.getId(), orderOne.getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }

    private void run2693(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SA20C50008".equals(caseData.getFamilyManCaseNumber())) {
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (caseData.getOrderCollection().size() < 10) {
                throw new IllegalArgumentException(format("Expected at least ten orders but found %s",
                    caseData.getOrderCollection().size()));
            }

            Element<GeneratedOrder> orderTen = caseData.getOrderCollection().get(9);

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, orderTen.getId(), orderTen.getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }

    private void run2724(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("WR20C50007".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<HearingOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        sealedCMORemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
    }
}
