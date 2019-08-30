package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.interfaces.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.EventValidationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;

@RequestMapping("/callback/notice-of-proceedings")
@Api
@RestController
public class NoticeOfProceedingsController {

    private final MapperService mapperService;
    private final EventValidationService eventValidationService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;

    @Autowired
    private NoticeOfProceedingsController(MapperService mapperService,
                                          EventValidationService eventValidationService,
                                          DocumentGeneratorService documentGeneratorService,
                                          UploadDocumentService uploadDocumentService,
                                          CaseDataExtractionService caseDataExtractionService) {
        this.mapperService = mapperService;
        this.eventValidationService = eventValidationService;
        this.documentGeneratorService = documentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("proceedingLabel", String.format("The case management hearing will be on the %s.",
            LocalDateTime.now().toString()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(eventValidationService.validateEvent(caseData, NoticeOfProceedingsGroup.class))
            .build();
    }

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitEvent(
            @RequestHeader(value = "authorization") String authorization,
            @RequestHeader(value = "user-id") String userId,
            @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        Map<String, String> placeholders = caseDataExtractionService
            .getNoticeOfProceedingTemplateData(caseData);
        List<DocmosisTemplates> templateTypes = getDocmosisTemplates(caseData);

        Map<String, Object> data = caseDetails.getData();

        templateTypes.stream()
            .flatMap(templateType -> Stream.of(documentGeneratorService.generateDocmosisDocument(placeholders,
                templateType)))
            .flatMap(docmosisDocument -> Stream.of(uploadDocumentService.uploadPDF(userId, authorization,
                docmosisDocument.getBytes(), docmosisDocument.getDocumentName())))
            .forEach(uploadedDocument -> {
                data.put(String.format("%s_document", uploadedDocument.originalDocumentName),
                    uploadedDocument.links.binary.href);
            });

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private List<DocmosisTemplates> getDocmosisTemplates(CaseData caseData) {
        List<DocmosisTemplates> templateTypes = Collections.emptyList();

        if (caseData.getProceedingTypes() != null
            && caseData.getProceedingTypes().contains(NOTICE_OF_PROCEEDINGS_FOR_PARTIES)) {
            templateTypes.add(C6);

        } else if (caseData.getProceedingTypes() != null
            && caseData.getProceedingTypes().contains(NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES)) {
            templateTypes.add(C6A);
        }

        return templateTypes;
    }
}
