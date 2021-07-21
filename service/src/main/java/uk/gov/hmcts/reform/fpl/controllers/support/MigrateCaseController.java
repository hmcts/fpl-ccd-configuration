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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            case "FPLA-3126":
                run3126(caseDetails);
                break;
            case "FPLA-3239":
                run3239(caseDetails);
                break;
            default:
                log.error("Unhandled migration {}", migrationId);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3126(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3126", "NE21C50026", caseData);

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

    private void run3239(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        validateFamilyManNumber("FPLA-3239", "DE21C50042", caseData);

        Optional<Element<SupportingEvidenceBundle>> correspondenceElement = caseData.getCorrespondenceDocuments()
            .stream()
            .filter(element -> "b1b7ef2d-b760-4961-aa5c-0ef9f5e40e95".equals(element.getId().toString()))
            .findAny();

        if (isNotEmpty(caseData.getSubmittedForm()) && correspondenceElement.isPresent()
            && correspondenceElement.get().getValue().getName().equals("Redacted C110a")) {
            caseDetails.getData().put("submittedForm", correspondenceElement.get().getValue().getDocument());

            caseData.getCorrespondenceDocuments().remove(correspondenceElement.get());
            caseDetails.getData().put("correspondenceDocuments", caseData.getCorrespondenceDocuments());
        } else {
            throw new IllegalStateException(format("Case %s does not have C110a/redacted copy", caseDetails.getId()));
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
