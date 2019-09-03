package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.interfaces.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.EventValidationService;
import uk.gov.hmcts.reform.fpl.service.MapperService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
            @RequestHeader(value = "authorization") String authorization,
            @RequestHeader(value = "user-id") String userId,
            @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapperService.mapObject(caseDetails.getData(), CaseData.class);

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData);

        List<DocmosisTemplates> templateTypes = getDocmosisTemplateTypes(caseData);

        List<Document> uploadedDocuments = generateAndUploadDocuments(userId, authorization, templateData,
            templateTypes);

        List<Map<Object, Object>> noticeOfProceedings = uploadedDocuments.stream()
            .map(document -> {
                return Element.builder()
                    .id(UUID.randomUUID())
                    .value(ImmutableMap.of(
                        "document", ImmutableMap.of(
                            "document_url", document.links.self.href
                            "document_filename", document.originalDocumentName
                            "document_binary_url", document.links.binary.href
                        )
                    ))
                    .build();
            }).collect(Collectors.toList());

        caseDetails.getData().put("noticeOfProceedingsBundle", noticeOfProceedings);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Document> generateAndUploadDocuments(String userId,
                                                      String authorization,
                                                      Map<String, String> templatePlaceholders,
                                                      List<DocmosisTemplates> templates) {
        List<DocmosisDocument> docmosisDocuments = templates.stream()
            .map(template -> documentGeneratorService.generateDocmosisDocument(templatePlaceholders, template))
            .collect(Collectors.toList());

        return docmosisDocuments.stream()
            .map(document -> uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
                document.getDocumentTitle() + ".pdf"))
            .collect(Collectors.toList());
    }

    private List<DocmosisTemplates> getDocmosisTemplateTypes(CaseData caseData) {
        ImmutableList.Builder<DocmosisTemplates> templateTypes = ImmutableList.builder();

        if (caseData.getProceedingTypes() != null
            && caseData.getProceedingTypes().contains(NOTICE_OF_PROCEEDINGS_FOR_PARTIES)) {
            templateTypes.add(C6);
        }

         if (caseData.getProceedingTypes() != null
            && caseData.getProceedingTypes().contains(NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES)) {
            templateTypes.add(C6A);
        }

        return templateTypes.build();
    }
}
