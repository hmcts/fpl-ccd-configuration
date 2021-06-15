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
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;

import java.util.List;
import java.util.UUID;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private final DocumentListService documentListService;

    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final String FAMILY_MAN_ID = "DE21C50016";
    private static final UUID DOCUMENT_TO_REMOVE_UUID = UUID.fromString("2acc1f5f-ff76-4c3e-b3fc-087ebebd2911");

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3135".equals(migrationId)) {
            run3135(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3135(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (FAMILY_MAN_ID.equals(caseData.getFamilyManCaseNumber())) {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();

            if (otherCourtAdminDocuments.stream().noneMatch(doc -> DOCUMENT_TO_REMOVE_UUID.equals(doc.getId()))) {
                throw new IllegalStateException(String
                    .format("Migration failed on case %s: Expected %s but not found",
                        caseData.getFamilyManCaseNumber(), DOCUMENT_TO_REMOVE_UUID));
            } else {
                otherCourtAdminDocuments.removeIf(document -> document.getId().equals(DOCUMENT_TO_REMOVE_UUID));
                caseDetails.getData().put("otherCourtAdminDocuments", otherCourtAdminDocuments);

                caseDetails.getData().putAll(documentListService.getDocumentView(getCaseData(caseDetails)));
            }
        }

    }
}
