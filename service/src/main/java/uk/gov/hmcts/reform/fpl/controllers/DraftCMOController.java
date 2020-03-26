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
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CMODocmosisTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.DraftCMOService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;

@Api
@RestController
@RequestMapping("/callback/draft-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final DraftCMOService draftCMOService;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CMODocmosisTemplateDataGenerationService docmosisTemplateDataGenerationService;
    private final RespondentService respondentService;
    private final OthersService othersService;
    private final CoreCaseDataService coreCaseDataService;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        draftCMOService.prepareCustomDirections(caseDetails, caseData.getCaseManagementOrder());

        caseDetails.getData().putAll(draftCMOService.extractIndividualCaseManagementOrderObjects(
            caseData.getCaseManagementOrder(), caseData.getHearingDetails()));

        caseDetails.getData().put("respondents_label", getRespondentsLabel(caseData));
        caseDetails.getData().put("others_label", getOthersLabel(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest)
        throws IOException {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();
        final CaseData caseData = mapper.convertValue(data, CaseData.class);

        Map<String, Object> cmoTemplateData = docmosisTemplateDataGenerationService.getTemplateData(caseData, true);

        Document document = getDocument(cmoTemplateData);

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

        data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), updatedCMO);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder populatedCMO = draftCMOService.prepareCMO(caseData, caseData.getCaseManagementOrder());

        draftCMOService.removeTransientObjectsFromCaseData(caseDetails.getData());

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), populatedCMO);

        caseDetails.getData().put("cmoEventId", DRAFT_CASE_MANAGEMENT_ORDER.getId());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    //TODO: logic for only calling this when necessary. When status change new vs old.
    // When new document to share.
    // When no data before.
    // FPLA-1471
    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change:CMO_PROGRESSION"
        );
    }

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthers(), Others.builder().build()));
    }

    private Document getDocument(Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, CMO);

        return uploadDocumentService.uploadPDF(document.getBytes(), "draft-" + document.getDocumentTitle());
    }
}
