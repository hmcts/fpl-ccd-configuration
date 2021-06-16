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
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private final DocumentListService documentListService;

    private static final String MIGRATION_ID_KEY = "migrationId";

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3092".equals(migrationId)) {
            run3092(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3092(CaseDetails caseDetails) {
        final String familyManId = "CF20C50063";
        final UUID documentToRemoveUUID = UUID.fromString("a1e1f56d-18b8-4123-acaf-7c276627628e");

        CaseData caseData = getCaseData(caseDetails);

        if (!Objects.equals(familyManId, caseData.getFamilyManCaseNumber())) {
            throw new AssertionError(String.format(
                "Migration FPLA-3092: Expected family man case number to be %s but was %s",
                familyManId, caseData.getFamilyManCaseNumber()));
        }

        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = caseData.getCorrespondenceDocuments();

        if (correspondenceDocuments.stream().noneMatch(doc -> documentToRemoveUUID.equals(doc.getId()))) {
            throw new IllegalStateException(String
                .format("Migration failed on case %s: Expected correspondence document id %s but not found",
                    caseData.getFamilyManCaseNumber(), documentToRemoveUUID));
        } else {
            correspondenceDocuments.removeIf(document -> documentToRemoveUUID.equals(document.getId()));
            caseDetails.getData().put("correspondenceDocuments", correspondenceDocuments);
        }
    }

}
