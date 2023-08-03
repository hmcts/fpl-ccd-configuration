package uk.gov.hmcts.reform.fpl.controllers.documents;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction.UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData.temporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Api
@RestController
@RequestMapping("/callback/manage-documentsv2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsControllerV2 extends CallbackController {

    private final ManageDocumentService manageDocumentService;

    @PostMapping("/manage-document-action-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleManageDocumentActionSelected(
        @RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("allowMarkDocumentConfidential", YesNo.from(manageDocumentService
            .allowMarkDocumentConfidential(caseData)));
        caseDetails.getData().put("allowSelectDocumentTypeToRemoveDocument", YesNo.from(manageDocumentService
            .allowSelectDocumentTypeToRemoveDocument(caseData)));
        caseDetails.getData().put("askForPlacementNoticeRecipientType", YesNo.from(DocumentUploaderType
            .HMCTS == manageDocumentService.getUploaderType(caseData)));
        caseDetails.getData().put("hasConfidentialParty", YesNo.from(caseData.hasConfidentialParty()));
        caseDetails.getData().put("uploadableDocumentBundle", List.of(
            element(UploadableDocumentBundle.builder()
                .documentTypeDynamicList(manageDocumentService.buildDocumentTypeDynamicList(
                    caseData))
                .placementList(asDynamicList(caseData.getPlacementEventData().getPlacements(), null,
                    Placement::getChildName))
                .build())
        ));

        return respond(caseDetails);
    }

    @PostMapping("/manage-document-upload-new-doc/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleManageDocumentNewDocumentUploaded(
        @RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();

        if (UPLOAD_DOCUMENTS.equals(eventData.getManageDocumentAction())
            && unwrapElements(eventData.getUploadableDocumentBundle()).stream().anyMatch(
                bundle -> !bundle.getDocumentTypeSelected().isUploadable())) {
            return respond(caseDetails,
                List.of("You are trying to upload a document to a parent folder, "
                    + "you need to choose one of the available sub folders."));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);

        Map<String, Object> updatedData = new HashMap<>();
        if (UPLOAD_DOCUMENTS.equals(eventData.getManageDocumentAction())) {
            updatedData.putAll(manageDocumentService.uploadDocuments(caseData));
        }
        caseDetailsMap.putAll(updatedData);
        removeTemporaryFields(caseDetailsMap, temporaryFields());

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        // TODO DFPL-1609 Notification Logic
    }
}
