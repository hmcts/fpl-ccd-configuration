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
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.barristers;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.designatedSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction.UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction.REMOVE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

@Api
@RestController
@RequestMapping("/callback/manage-documentsv2")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsControllerV2 extends CallbackController {

    private final UserService userService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/manage-document-action-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleManageDocumentActionSelected(
        @RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();

        caseDetails.getData().put("uploadableDocumentBundle", List.of(ElementUtils.element(UploadableDocumentBundle.builder()
            .documentTypeDynamicList(DynamicList.builder().listItems(
                List.of(DynamicListElement.builder().code("THRESHOLD").label("Threshold").build())
            ).build())
            .build())));


        return respond(caseDetails);
    }

    @PostMapping("/manage-document-upload-new-doc/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleManageDocumentNewDocumentUploaded(
        @RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();
        ManageDocumentAction action = eventData.getManageDocumentAction();

        if (UPLOAD_DOCUMENTS.equals(action)) {
            eventData.getUploadableDocumentBundle().stream().forEach(ud -> {
                boolean isConfidential = YesNo.YES.getValue().equals(ud.getValue().getConfidential());
            });
        } else if (REMOVE_DOCUMENTS.equals(action)) {
            // TODO
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails);
        boolean isSolicitor = DocumentUploaderType.SOLICITOR.equals(getUploaderType(caseData.getId()));

        // TODO

        return respond(caseDetailsMap);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        // TODO Notification Logic
        /*
        CaseData caseData = getCaseData(request);

        DocumentUploaderType userType = getUploaderType(caseData.getId());

        if (this.featureToggleService.isNewDocumentUploadNotificationEnabled()
            || (!DocumentUploaderType.SOLICITOR.equals(userType) && !DocumentUploaderType.BARRISTER.equals(userType))) {
            UserDetails userDetails = userService.getUserDetails();

            publishEvent(new FurtherEvidenceUploadedEvent(getCaseData(request),
                getCaseDataBefore(request), userType, userDetails));
        }
        */
    }

    private DocumentUploaderType getUploaderType(Long id) {
        final Set<CaseRole> caseRoles = userService.getCaseRoles(id);
        if (caseRoles.stream().anyMatch(representativeSolicitors()::contains)) {
            return DocumentUploaderType.SOLICITOR;
        }
        if (caseRoles.stream().anyMatch(barristers()::contains)) {
            return DocumentUploaderType.BARRISTER;
        }
        if (caseRoles.contains(LASHARED)) {
            return DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
        }
        if (userService.isHmctsUser()) {
            return DocumentUploaderType.HMCTS;
        }
        if (caseRoles.stream().anyMatch(designatedSolicitors()::contains)) {
            return DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
        }
        throw new RuntimeException("unresolved document uploader type");
    }
}
