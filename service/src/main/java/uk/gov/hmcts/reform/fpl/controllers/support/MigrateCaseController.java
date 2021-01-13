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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.document.UploadDocumentsMigrationService;
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;
import uk.gov.hmcts.reform.fpl.service.removeorder.GeneratedOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.Map;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {

    private final UploadDocumentsMigrationService uploadDocumentsMigrationService;
    private final StandardDirectionsService standardDirectionsService;
    private final CMORemovalAction cmoRemovalAction;
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;
    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2379".equals(migrationId)) {
            run2379(caseDetails);
        }

        if ("FPLA-2544".equals(migrationId)) {
            run2544(caseDetails);
        }
      
        if ("FPLA-2521".equals(migrationId)) {
            run2521(caseDetails);
        }

        if ("FPLA-2535".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2535(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2535(CaseDetails caseDetails) {
        if ("1607361111762499".equals(caseDetails.getId().toString())) {
            CaseData caseData = getCaseData(caseDetails);
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (isEmpty(caseData.getOrderCollection())) {
                throw new IllegalArgumentException("No generated orders in the case");
            }

            Element<GeneratedOrder> firstOrder = caseData.getOrderCollection().get(0);

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, firstOrder.getId(), firstOrder.getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }

    private void run2521(CaseDetails caseDetails) {
        if ("1599470847274974".equals(caseDetails.getId().toString())) {
            CaseData caseData = getCaseData(caseDetails);

            if (isEmpty(caseData.getDraftUploadedCMOs())) {
                throw new IllegalArgumentException("No draft case management orders in the case");
            }

            Element<CaseManagementOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

            cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
        }
    }

    private void run2379(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        log.info("Migration of Old Documents to Application Documents for case ID {}",
            caseData.getId());
        Map<String, Object> data = caseDetails.getData();
        data.putAll(uploadDocumentsMigrationService.transformFromOldCaseData(caseData));
    }

    private void run2544(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("PO20C50030".equals(caseData.getFamilyManCaseNumber())) {
            if (!SUBMITTED.equals(caseData.getState())) {
                throw new IllegalStateException(
                    format("Case is in %s state, expected %s", caseData.getState(), SUBMITTED));
            }

            caseDetails.getData().put("state", State.GATEKEEPING);
            caseDetails.getData().putAll(standardDirectionsService.populateStandardDirections(caseData));
        }
    }

}
