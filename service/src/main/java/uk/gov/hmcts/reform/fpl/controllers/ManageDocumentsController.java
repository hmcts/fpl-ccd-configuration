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
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.AMEND;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.DELETE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getSelectedIdFromDynamicList;

@Api
@RestController
@RequestMapping("/callback/manage-docs")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentsController {

    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put("pageShow", caseData.getOtherCourtAdminDocuments().size() != 0 ? "Yes" : "No");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/populate-list/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateList(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        List<Element<CourtAdminDocument>> courtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter uploadDocumentsRouter = caseData.getUploadDocumentsRouter();

        if (AMEND == uploadDocumentsRouter || DELETE == uploadDocumentsRouter) {
            DynamicList courtDocumentList = asDynamicList(
                courtAdminDocuments,
                CourtAdminDocument::getDocumentTitle
            );

            data.put("courtDocumentList", courtDocumentList);
        }

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
        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter router = caseData.getUploadDocumentsRouter();

        Object courtDocumentList = caseData.getCourtDocumentList();

        UUID selectedId = getSelectedIdFromDynamicList(courtDocumentList, mapper);

        findElement(selectedId, otherCourtAdminDocuments).ifPresent(
            courtAdminDocument -> {
                if (AMEND == router) {
                    data.put("originalCourtDocument", courtAdminDocument.getValue().getDocument());
                } else {
                    data.put("deletedCourtDocument", courtAdminDocument.getValue());
                }
            }
        );

        DynamicList regeneratedList = asDynamicList(
            otherCourtAdminDocuments,
            selectedId,
            CourtAdminDocument::getDocumentTitle
        );

        data.put("courtDocumentList", regeneratedList);

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
        List<Element<CourtAdminDocument>> otherCourtAdminDocuments = caseData.getOtherCourtAdminDocuments();
        DocumentRouter router = caseData.getUploadDocumentsRouter();

        // Will be null if pageShow was NO in which case upload is the only option
        if (UPLOAD == router || null == router) {
            List<Element<CourtAdminDocument>> limitedCourtAdminDocuments = caseData.getLimitedCourtAdminDocuments();
            otherCourtAdminDocuments.addAll(limitedCourtAdminDocuments);
        } else {
            Object courtDocumentList = caseData.getCourtDocumentList();
            UUID selectedId = getSelectedIdFromDynamicList(courtDocumentList, mapper);
            int index = -1;
            for (int i = 0; i < otherCourtAdminDocuments.size(); i++) {
                Element<?> element = otherCourtAdminDocuments.get(i);
                if (selectedId.equals(element.getId())) {
                    index = i;
                }
            }
            if (AMEND == router) {
                Element<CourtAdminDocument> editedDocument = element(selectedId, caseData.getEditedCourtDocument());
                otherCourtAdminDocuments.set(index, editedDocument);
            } else if (DELETE == router) {
                otherCourtAdminDocuments.remove(index);
            }
        }

        data.put("otherCourtAdminDocuments", otherCourtAdminDocuments);
        removeTemporaryFields(
            caseDetails,
            "limitedCourtAdminDocuments",
            "editedCourtDocument",
            "deletedCourtDocument",
            "courtDocumentList",
            "uploadDocumentsRouter",
            "originalCourtDocument",
            "pageShow"
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
