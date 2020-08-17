package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.service.ManageDocumentsService;

import java.util.Map;

import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;

@Api
@RestController
@RequestMapping("/callback/manage-docs")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentsController {

    private final ObjectMapper mapper;
    private final ManageDocumentsService service;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("pageShow", !caseData.getOtherCourtAdminDocuments().isEmpty() ? "Yes" : "No");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/populate-list/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateList(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("courtDocumentList", service.buildDocumentDynamicList(caseData));

        // needed to prevent issues when using previous button
        // is affected by RDM-9147
        CourtAdminDocument editedDocument = caseData.getEditedCourtDocument();
        if (editedDocument != null && editedDocument.getDocument() != null && editedDocument.getDocument().isEmpty()) {
            data.remove("editedCourtDocument");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/get-doc/mid-event")
    public AboutToStartOrSubmitCallbackResponse getDocument(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(service.getDocumentToDisplay(caseData));
        data.put("courtDocumentList", service.regenerateDynamicList(caseData));

        // needed to prevent issues when using previous button
        // is affected by RDM-9147
        CourtAdminDocument editedDocument = caseData.getEditedCourtDocument();
        if (editedDocument != null && editedDocument.getDocument() != null && editedDocument.getDocument().isEmpty()) {
            data.remove("editedCourtDocument");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("otherCourtAdminDocuments", service.updateDocumentCollection(caseData));
        removeTemporaryFields(
            caseDetails,
            "limitedCourtAdminDocuments",
            "editedCourtDocument",
            "deletedCourtDocument",
            "courtDocumentList",
            "manageDocumentsAction",
            "originalCourtDocument",
            "pageShow"
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
