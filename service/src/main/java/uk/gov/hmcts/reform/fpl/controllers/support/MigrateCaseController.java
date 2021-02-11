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
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;
import uk.gov.hmcts.reform.fpl.service.removeorder.GeneratedOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static java.lang.String.format;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;
    private final CMORemovalAction cmoRemovalAction;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2693".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2693(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        if("FPLA-2716".equals(migrationId)) {
            run2716(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2716(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF21C50009".equals(caseData.getFamilyManCaseNumber())) {
            removeC21OrderFromFirstHearingOrderBundle(caseDetails);
        }
    }

    private void removeC21OrderFromFirstHearingOrderBundle(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        List<Element<HearingOrder>> hearingOrders = caseData.getHearingOrdersBundlesDrafts().get(0).getValue().getOrders();
        hearingOrders.remove(1);

        List<Element<HearingOrdersBundle>> updatedBundle = caseData.getHearingOrdersBundlesDrafts();
        updatedBundle.get(0).getValue().setOrders(hearingOrders);

        caseDetails.getData().put("hearingOrdersBundlesDrafts", updatedBundle);
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
}
