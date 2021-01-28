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
import uk.gov.hmcts.reform.fpl.service.removeorder.CMORemovalAction;

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

        if ("FPLA-2637".equals(migrationId)) {
            run2637(caseDetails);
        }

        if ("FPLA-2640".equals(migrationId)) {
            run2640(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2637(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("LE20C50024".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2640(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("NE20C50006".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<HearingOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
    }
}
