package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.interfaces.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.time.format.FormatStyle;

import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

@RequestMapping("/callback/notice-of-proceedings")
@Api
@RestController
public class NoticeOfProceedingsController {

    private final ObjectMapper mapper;
    private final ValidateGroupService eventValidationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingBookingService hearingBookingService;
    private final DateFormatterService dateFormatterService;

    @Autowired
    private NoticeOfProceedingsController(ObjectMapper mapper,
                                          ValidateGroupService eventValidationService,
                                          DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                          UploadDocumentService uploadDocumentService,
                                          CaseDataExtractionService caseDataExtractionService,
                                          HearingBookingService hearingBookingService,
                                          DateFormatterService dateFormatterService) {
        this.mapper = mapper;
        this.eventValidationService = eventValidationService;
        this.docmosisDocumentGeneratorService = docmosisDocumentGeneratorService;
        this.uploadDocumentService = uploadDocumentService;
        this.caseDataExtractionService = caseDataExtractionService;
        this.hearingBookingService = hearingBookingService;
        this.dateFormatterService = dateFormatterService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getFamilyManCaseNumber() != null && caseData.getHearingDetails() != null) {
            HearingBooking hearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

            caseDetails.getData().put("proceedingLabel", String.format("The case management hearing will be on the %s.",
                dateFormatterService.formatLocalDateToString(hearingBooking.getDate(), FormatStyle.LONG)));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
            @RequestHeader(value = "authorization") String authorization,
            @RequestHeader(value = "user-id") String userId,
            @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Map<String, String> templateData = caseDataExtractionService
            .getNoticeOfProceedingTemplateData(caseData);

        List<DocmosisTemplates> templateTypes = getProceedingTemplateTypes(caseData);

        List<Document> uploadedDocuments = generateAndUploadDocuments(userId, authorization, templateData,
            templateTypes);

        List<Element> documentsBundle = uploadedDocuments.stream()
            .map(document -> {
                return Element.builder()
                    .id(UUID.randomUUID())
                    .value(ImmutableMap.builder()
                        .put("document", DocumentReference.builder()
                            .document_filename(document.originalDocumentName)
                            .document_url(document.links.self.href)
                            .document_binary_url(document.links.binary.href)
                            .build())
                        .build())
                    .build();
            }).collect(Collectors.toList());

        caseDetails.getData().put("noticeOfProceedingsBundle", documentsBundle);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private List<Document> generateAndUploadDocuments(String userId,
                                                      String authorization,
                                                      Map<String, String> templatePlaceholders,
                                                      List<DocmosisTemplates> templates) {
        List<DocmosisDocument> docmosisDocuments = templates.stream()
            .map(template -> docmosisDocumentGeneratorService.generateDocmosisDocument(templatePlaceholders, template))
            .collect(Collectors.toList());

        return docmosisDocuments.stream()
            .map(document -> uploadDocumentService.uploadPDF(userId, authorization, document.getBytes(),
                document.getDocumentTitle() + ".pdf"))
            .collect(Collectors.toList());
    }

    private List<DocmosisTemplates> getProceedingTemplateTypes(CaseData caseData) {
        return caseData.getProceedingTypes().stream()
            .map(DocmosisTemplates::getFromProceedingType)
            .collect(Collectors.toList());
    }
}
