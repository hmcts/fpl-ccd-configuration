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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);

        log.info("Migration " + migrationId);

        switch (migrationId) {
            case "FPLA-3262":
                run3262(caseDetails);
                break;
            default:
                log.error("Unhandled migration {}", migrationId);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3262(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3262", "PE21C50004", caseData);

        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs();
        if (isNotEmpty(draftUploadedCMOs) && draftUploadedCMOs.size() == 1) {
            caseDetails.getData().remove("draftUploadedCMOs");
        } else {
            throw new IllegalStateException(format("Case %s does not contain 1 draft CMO", caseDetails.getId()));
        }

        List<Element<HearingBooking>> cancelledHearingDetails = caseData.getCancelledHearingDetails();
        if (isNotEmpty(cancelledHearingDetails) && cancelledHearingDetails.size() == 1
            && cancelledHearingDetails.get(0).getValue().getCaseManagementOrderId().equals(
            draftUploadedCMOs.get(0).getId())) {
            cancelledHearingDetails.get(0).getValue().setCaseManagementOrderId(null);
            caseDetails.getData().put("cancelledHearingDetails", cancelledHearingDetails);
        } else {
            throw new IllegalStateException(
                format("Case %s has unexpected cancelled hearing details", caseDetails.getId()));
        }
    }

    private void validateFamilyManNumber(String migrationId, String familyManCaseNumber, CaseData caseData) {
        if (!Objects.equals(familyManCaseNumber, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(format(
                "Migration %s: Expected family man case number to be %s but was %s",
                migrationId, familyManCaseNumber, caseData.getFamilyManCaseNumber()));
        }
    }

}
