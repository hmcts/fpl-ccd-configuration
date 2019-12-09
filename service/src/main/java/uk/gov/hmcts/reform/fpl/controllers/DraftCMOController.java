package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CMODocmosisTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.io.IOException;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CMODocmosisTemplateDataGenerationService docmosisTemplateDataGenerationService;

    @Autowired
    public DraftCMOController(ObjectMapper mapper,
                              DraftCMOService draftCMOService,
                              DocmosisDocumentGeneratorService docmosisService,
                              UploadDocumentService uploadDocumentService,
                              CMODocmosisTemplateDataGenerationService docmosisTemplateDataGenerationService) {
        this.mapper = mapper;
        this.draftCMOService = draftCMOService;
        this.docmosisService = docmosisService;
        this.uploadDocumentService = uploadDocumentService;
        this.docmosisTemplateDataGenerationService = docmosisTemplateDataGenerationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        draftCMOService.prepareCustomDirections(caseDetails.getData());

        setCustomDirectionDropdownLabels(caseDetails);
        Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestHeader("authorization") String authorization,
                                                               @RequestHeader("user-id") String userId,
                                                               @RequestBody CallbackRequest callbackRequest)
        throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        final Map<String, Object> cmoTemplateData = docmosisTemplateDataGenerationService.getTemplateData(caseData);

        Document document = getDocument(authorization, userId, cmoTemplateData);

        final DocumentReference reference = DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();

        final CaseManagementOrder oldCMO = defaultIfNull(
            caseData.getCaseManagementOrder(), CaseManagementOrder.builder().build());

        final CaseManagementOrder updatedCMO = oldCMO.toBuilder()
            .orderDoc(reference)
            .build();

        data.put("caseManagementOrder", updatedCMO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        CaseManagementOrder caseManagementOrder = draftCMOService.prepareCMO(caseData);

        draftCMOService.removeTransientObjectsFromCaseData(data);
        draftCMOService.populateCaseDataWithCMO(data, caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private void setCustomDirectionDropdownLabels(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getOthers() != null) {
            caseDetails.getData().put("otherPartiesDropdownLabelCMO",
                draftCMOService.createOtherPartiesAssigneeDropdownKey(caseData.getOthers()));
        }

        caseDetails.getData().put("respondentsDropdownLabelCMO",
            draftCMOService.createRespondentAssigneeDropdownKey(caseData.getRespondents1()));
    }

    private Document getDocument(String authorization, String userId, Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, DocmosisTemplates.CMO);

        String docTitle = document.getDocumentTitle();

        if (isNotEmpty(templateData.get("draftbackground"))) {
            docTitle = "draft-" + document.getDocumentTitle();
        }

        return uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(), docTitle);
    }

}
