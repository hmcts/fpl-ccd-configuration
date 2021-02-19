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
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.removeorder.SealedCMORemovalAction;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.C2_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.CORRESPONDING_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final ConfidentialDocumentsSplitter splitter;
    private final SealedCMORemovalAction sealedCMORemovalAction;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Object migrationId = caseDetails.getData().get(MIGRATION_ID_KEY);

        if ("FPLA-2724".equals(migrationId)) {
            run2724(caseDetails);
        }

        if ("FPLA-2705".equals(migrationId)) {
            run2705(caseDetails);
        }

        if ("FPLA-2706".equals(migrationId)) {
            run2706(caseDetails);
        }

        if ("FPLA-2722".equals(migrationId)) {
            log.info("Performing migration ({}) for case {}", migrationId, caseDetails.getId());
            run2417(caseDetails);
        }

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run2417(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put(
            HEARING_FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY, caseData.getHearingFurtherEvidenceDocuments()
        );
        caseDetails.getData().put(
            C2_DOCUMENTS_COLLECTION_KEY, caseData.getC2DocumentBundle()
        );
        caseDetails.getData().putAll(updateSupportingDocs(
            caseData.getFurtherEvidenceDocuments(), FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_KEY
        ));
        caseDetails.getData().putAll(updateSupportingDocs(
            caseData.getFurtherEvidenceDocumentsLA(), FURTHER_EVIDENCE_DOCUMENTS_COLLECTION_LA_KEY
        ));
        caseDetails.getData().putAll(updateSupportingDocs(
            caseData.getCorrespondenceDocuments(), CORRESPONDING_DOCUMENTS_COLLECTION_KEY
        ));
        caseDetails.getData().putAll(updateSupportingDocs(
            caseData.getCorrespondenceDocumentsLA(), CORRESPONDING_DOCUMENTS_COLLECTION_LA_KEY
        ));
    }

    private Map<String, Object> updateSupportingDocs(List<Element<SupportingEvidenceBundle>> supportingDocs,
                                                     String key) {
        if (isEmpty(supportingDocs)) {
            return Map.of();
        }

        return splitter.splitIntoAllAndNonConfidential(supportingDocs, key);
    }

    private void run2706(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("CF20C50049".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2724(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("WR20C50007".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void run2705(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if ("SN20C50023".equals(caseData.getFamilyManCaseNumber())) {
            removeFirstDraftCaseManagementOrder(caseDetails);
        }
    }

    private void removeFirstDraftCaseManagementOrder(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getDraftUploadedCMOs())) {
            throw new IllegalArgumentException("No draft case management orders in the case");
        }

        Element<HearingOrder> firstDraftCmo = caseData.getDraftUploadedCMOs().get(0);

        sealedCMORemovalAction.removeDraftCaseManagementOrder(caseData, caseDetails, firstDraftCmo);
    }
}
