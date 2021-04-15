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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2982".equals(migrationId)) {
            run2982(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2982(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle =
            caseData.getAdditionalApplicationsBundle();

        if (additionalApplicationsBundle.stream()
            .noneMatch(bundle -> bundle.getValue().getC2DocumentBundle().getId() == null)) {
            throw new IllegalArgumentException("No c2DocumentBundle found with missing Id");
        }

        List<Element<AdditionalApplicationsBundle>> fixedAdditionalApplicationsBundle =
            additionalApplicationsBundle.stream().map(this::fixMissingId).collect(Collectors.toList());
        caseDetails.getData().put("additionalApplicationsBundle", fixedAdditionalApplicationsBundle);
    }

    private Element<AdditionalApplicationsBundle> fixMissingId(Element<AdditionalApplicationsBundle> bundle) {
        C2DocumentBundle c2DocumentBundle = bundle.getValue().getC2DocumentBundle();

        if (c2DocumentBundle.getId() == null) {

            /*
            C2DocumentBundle fixedC2DocumentBundle = C2DocumentBundle.builder()
                .id(UUID.randomUUID())
                .author(c2DocumentBundle.getAuthor())
                .c2AdditionalOrdersRequested(c2DocumentBundle.getC2AdditionalOrdersRequested())
                .clientCode(c2DocumentBundle.getClientCode())
                .description(c2DocumentBundle.getDescription())
                .document(c2DocumentBundle.getDocument())
                .fileReference(c2DocumentBundle.getFileReference())
                .nameOfRepresentative(c2DocumentBundle.getNameOfRepresentative())
                .parentalResponsibilityType(c2DocumentBundle.getParentalResponsibilityType())
                .pbaNumber(c2DocumentBundle.getPbaNumber())
                .supplementsBundle(c2DocumentBundle.getSupplementsBundle())
                .supportingEvidenceBundle(c2DocumentBundle.getSupportingEvidenceBundle())
                .type(c2DocumentBundle.getType())
                .uploadedDateTime(c2DocumentBundle.getUploadedDateTime())
                .usePbaPayment(c2DocumentBundle.getUsePbaPayment())
                .build();

                bundle.getValue().setC2DocumentBundle(fixedC2DocumentBundle);
             */

            bundle.getValue().getC2DocumentBundle().setId(UUID.randomUUID());
        }

        return bundle;
    }
}
