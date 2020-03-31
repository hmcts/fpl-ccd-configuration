package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.ProceedingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.service.DateFormatterService.formatLocalDateToString;

@Api
@RestController
@RequestMapping("/callback/notice-of-proceedings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsController {
    private final ObjectMapper mapper;
    private final ValidateGroupService eventValidationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final NoticeOfProceedingsService noticeOfProceedingsService;
    private final HearingBookingService hearingBookingService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class).isEmpty()) {
            hearingBookingService.getFirstHearing(caseData.getHearingDetails())
                .ifPresent(hearingBooking ->
                    caseDetails.getData().put("proceedingLabel", buildProceedingLabel(hearingBooking)));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class))
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        Map<String, Object> templateData = noticeOfProceedingsService.getNoticeOfProceedingTemplateData(caseData);

        List<DocmosisTemplates> templateTypes = getProceedingTemplateTypes(caseData);

        List<Document> uploadedDocuments = generateAndUploadDocuments(templateData, templateTypes);

        List<Element<DocumentBundle>> noticeOfProceedingCaseData = createNoticeOfProceedingsCaseData(uploadedDocuments);

        if (isNotEmpty(callbackRequest.getCaseDetailsBefore().getData().get("noticeOfProceedingsBundle"))) {
            CaseData caseDataBefore = mapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(),
                CaseData.class);

            noticeOfProceedingCaseData.addAll(noticeOfProceedingsService
                .getRemovedDocumentBundles(caseDataBefore, templateTypes));
        }

        caseDetails.getData().put("noticeOfProceedingsBundle", noticeOfProceedingCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private String buildProceedingLabel(HearingBooking hearingBooking) {
        return String.format("The case management hearing will be on the %s.",
            formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG));
    }

    private List<Element<DocumentBundle>> createNoticeOfProceedingsCaseData(List<Document> uploadedDocuments) {
        return uploadedDocuments.stream()
            .map(document -> Element.<DocumentBundle>builder()
                .id(UUID.randomUUID())
                .value(DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename(document.originalDocumentName)
                        .url(document.links.self.href)
                        .binaryUrl(document.links.binary.href)
                        .build())
                    .build())
                .build())
            .collect(Collectors.toList());
    }

    private List<Document> generateAndUploadDocuments(Map<String, Object> templatePlaceholders,
                                                      List<DocmosisTemplates> templates) {
        List<DocmosisDocument> docmosisDocuments = templates.stream()
            .map(template -> docmosisDocumentGeneratorService.generateDocmosisDocument(templatePlaceholders, template))
            .collect(Collectors.toList());

        return docmosisDocuments.stream()
            .map(document -> uploadDocumentService.uploadPDF(document.getBytes(), document.getDocumentTitle()))
            .collect(Collectors.toList());
    }

    private List<DocmosisTemplates> getProceedingTemplateTypes(CaseData caseData) {
        ImmutableList.Builder<DocmosisTemplates> proceedingTypes = ImmutableList.builder();

        if (caseData.getNoticeOfProceedings().getProceedingTypes()
            .contains(ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_PARTIES)) {
            proceedingTypes.add(DocmosisTemplates.C6);
        }

        if (caseData.getNoticeOfProceedings().getProceedingTypes()
            .contains(ProceedingType.NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES)) {
            proceedingTypes.add(DocmosisTemplates.C6A);
        }

        return proceedingTypes.build();
    }
}
