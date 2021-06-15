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
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
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

    private static final UUID BUNDLE_ID = UUID.fromString("b02898e7-46dc-47ce-9639-9e5b04d03b9e");
    private static final UUID C2_ID = UUID.fromString("4b725c8a-3496-4f28-83f1-95d4838a533a");
    private static final String C2_DOC_ID = "b444c4fb-362b-4e27-b7d8-61996b3f6e0d";
    private static final String FAMILY_MAN_CASE_NUMBER = "SA20C50050";
    private static final int BUNDLE_SIZE = 2;

    private static final String FAMILY_MAN_ID = "DE21C50016";
    private static final UUID DOCUMENT_TO_REMOVE_UUID = UUID.fromString("2acc1f5f-ff76-4c3e-b3fc-087ebebd2911");

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-3135".equals(migrationId)) {
            run3135(caseDetails);
        }

        if ("FPLA-3125".equals(migrationId)) {
            run3125(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run3125(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (!Objects.equals(FAMILY_MAN_CASE_NUMBER, caseData.getFamilyManCaseNumber())) {
            throwException("family man case number", FAMILY_MAN_CASE_NUMBER, caseData.getFamilyManCaseNumber());
        }

        List<Element<AdditionalApplicationsBundle>> bundles = caseData.getAdditionalApplicationsBundle();

        if (BUNDLE_SIZE != bundles.size()) {
            throwException("additional applications bundle size", BUNDLE_SIZE, bundles.size());
        }

        Element<AdditionalApplicationsBundle> bundle = bundles.get(1);

        validateBundle(bundle);

        bundles.remove(1);

        caseDetails.getData().put("additionalApplicationsBundle", bundles);
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

    private void validateBundle(Element<AdditionalApplicationsBundle> bundle) {
        if (!Objects.equals(BUNDLE_ID, bundle.getId())) {
            throwException("bundle id", BUNDLE_ID, bundle.getId());
        }

        C2DocumentBundle c2Bundle = bundle.getValue().getC2DocumentBundle();

        if (!Objects.equals(C2_ID, c2Bundle.getId())) {
            throwException("c2 id", C2_ID, c2Bundle.getId());
        }

        String docUrl = c2Bundle.getDocument().getUrl();

        if (!docUrl.contains(C2_DOC_ID)) {
            throwException("doc id", C2_DOC_ID, docUrl);
        }
    }

    private void throwException(String field, Object expected, Object actual) {
        throw new AssertionError(String.format(
            "Migration FPLA-3125: Expected %s to be %s but was %s", field, expected, actual
        ));
    }
}
