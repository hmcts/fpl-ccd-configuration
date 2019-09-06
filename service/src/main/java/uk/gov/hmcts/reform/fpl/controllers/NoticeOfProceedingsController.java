package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.service.DocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.EventValidationService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;

import java.time.LocalDate;
import java.time.format.FormatStyle;

import java.util.List;
import java.util.Map;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES;

@RequestMapping("/callback/notice-of-proceedings")
@Api
@RestController
public class NoticeOfProceedingsController {

    private final ObjectMapper mapper;
    private final EventValidationService eventValidationService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingBookingService hearingBookingService;
    private final DateFormatterService dateFormatterService;

    @Autowired
    private NoticeOfProceedingsController(ObjectMapper mapper,
                                          EventValidationService eventValidationService,
                                          DocumentGeneratorService documentGeneratorService,
                                          DocmosisDocumentGeneratorService docmosisDocumentGeneratorService,
                                          UploadDocumentService uploadDocumentService,
                                          CaseDataExtractionService caseDataExtractionService,
                                          HearingBookingService hearingBookingService,
                                          DateFormatterService dateFormatterService) {
        this.mapper = mapper;
        this.eventValidationService = eventValidationService;
        this.documentGeneratorService = documentGeneratorService;
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

        HearingBooking hearingBooking = hearingBookingService.getMostUrgentHearingBooking(caseData);

        String formattedDate = dateFormatterService.formatLocalDateToString(hearingBooking.getDate() != null
            ? hearingBooking.getDate() : LocalDate.now(), FormatStyle.LONG);

        caseDetails.getData().put("proceedingLabel", String.format("The case management hearing will be on the %s.",
            formattedDate));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(eventValidationService.validateEvent(caseData, NoticeOfProceedingsGroup.class))
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
            .getNoticeOfProceedingTemplateData(caseData, caseDetails.getJurisdiction());

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
