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

        if ("FPLA-2480".equals(migrationId)) {
            run2480(caseDetails);
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

    private void removeDraftCaseManagementOrder(CaseDetails caseDetails, int index) {
        CaseData caseData = getCaseData(caseDetails);
        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }
        Element<CaseManagementOrder> draftCmo = caseData.getDraftUploadedCMOs().get(index);
        cmoRemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, draftCmo);
    }
}
