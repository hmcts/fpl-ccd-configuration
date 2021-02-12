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
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.service.removeorder.GeneratedOrderRemovalAction;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final GeneratedOrderRemovalAction generatedOrderRemovalAction;

    private final DraftOrderService draftOrderService;

    private static final List<Long> casesWithUploadDraftCMOs = List.of(
        1609255930041507L, 1606987766244887L, 1604311638012517L, 1610617556504448L, 1611053637858331L,
        1611831571219051L, 1612440806991994L, 1611613172339094L, 1597234670803750L, 1610638275960711L,
        1612792241936277L, 1608227716601946L, 1606816433160806L, 1604488701821402L, 1612260529074989L,
        1602684664094829L, 1611162463937927L, 1603112430374845L, 1609332375073780L, 1606920447453254L,
        1603186601101963L, 1601047439875628L, 1607700990753436L, 1593751852182152L, 1599567482297796L,
        1600763666484840L, 1608550125506632L, 1611073519965954L, 1601036838115652L, 1600681449523496L,
        1604665756707715L, 1605864027624648L, 1604698798063326L, 1601466075213425L, 1606835150877657L,
        1600924712703100L, 1602695126592366L, 1610448987787679L, 1604057238263139L, 1607650998329954L,
        1602072210447867L, 1607089490115787L, 1605252849637867L);

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2684".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2684(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        if ("FPLA-2687".equals(migrationId)) {
            Object hiddenOrders = caseDetails.getData().get("hiddenOrders");
            run2687(caseDetails);
            caseDetails.getData().put("hiddenOrders", hiddenOrders);
        }

        if ("FPLA-2710".equals(migrationId)) {
            CaseData caseData = getCaseData(caseDetails);
            if (casesWithUploadDraftCMOs.contains(caseData.getId()) && isNotEmpty(caseData.getDraftUploadedCMOs())
                && isEmpty(caseData.getHearingOrdersBundlesDrafts())) {
                log.info(
                    "Migrating draft CMOs to Hearing orders draft bundles - case reference {} Number of Draft CMOs {}",
                    caseData.getId(), caseData.getDraftUploadedCMOs().size());

                List<Element<HearingOrdersBundle>> migratedBundles =
                    draftOrderService.migrateCmoDraftToOrdersBundles(caseData);
                caseDetails.getData().put("hearingOrdersBundlesDrafts", migratedBundles);

                log.info(
                    "Completed migration to Hearing orders draft bundles. case reference {} Number of bundles {}",
                    caseData.getId(), migratedBundles.size());
            }
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2684(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50070".equals(caseData.getFamilyManCaseNumber())) {
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (caseData.getOrderCollection().size() < 3) {
                throw new IllegalArgumentException(format("Expected at least three orders but found %s",
                    caseData.getOrderCollection().size()));
            }

            Element<GeneratedOrder> orderThree = caseData.getOrderCollection().get(2);

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, orderThree.getId(), orderThree.getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }

    private void run2687(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50009".equals(caseData.getFamilyManCaseNumber())) {
            CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

            if (caseData.getOrderCollection().size() < 3) {
                throw new IllegalArgumentException(format("Expected at least three orders but found %s",
                    caseData.getOrderCollection().size()));
            }

            Element<GeneratedOrder> orderThree = caseData.getOrderCollection().get(2);

            generatedOrderRemovalAction.remove(caseData, caseDetailsMap, orderThree.getId(), orderThree.getValue());

            caseDetails.setData(caseDetailsMap);
        }
    }
}
