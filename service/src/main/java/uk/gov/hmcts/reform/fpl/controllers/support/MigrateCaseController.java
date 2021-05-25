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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final CaseSubmissionService caseSubmissionService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3037".equals(migrationId)) {
            run3037(caseDetails);
        }

        if ("FPLA-3087".equals(migrationId)) {
            run3087(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3037(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles =
            caseData.getAdditionalApplicationsBundle();

        additionalApplicationsBundles.forEach(
            additionalApplicationsBundle -> {
                C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getValue().getC2DocumentBundle();

                c2DocumentBundle.getSupportingEvidenceBundle()
                    .removeIf(supportingEvidenceBundle -> supportingEvidenceBundle.getId()
                        .toString()
                        .equals("4885a0e2-fd88-4614-9c35-6c61d6b5e422")
                    );
            }
        );

        data.put("additionalApplicationsBundle", additionalApplicationsBundles);
        caseDetails.setData(data);
    }

    private void run3087(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        if (1618497329043582L == caseData.getId()) {
            log.info("Regenerating C110a for {}", caseData.getId());
            Document document = caseSubmissionService.generateSubmittedFormPDF(caseData, false);
            log.info("Regenerated C110a for {}", caseData.getId());
            caseDetails.getData().put("submittedForm", DocumentReference.buildFromDocument(document));
        }
    }
}
