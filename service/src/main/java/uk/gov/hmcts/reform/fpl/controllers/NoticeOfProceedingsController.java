package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;

import java.time.format.FormatStyle;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Api
@RestController
@RequestMapping("/callback/notice-of-proceedings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsController extends CallbackController {
    private final ValidateGroupService eventValidationService;
    private final DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;
    private final UploadDocumentService uploadDocumentService;
    private final NoticeOfProceedingsService noticeOfProceedingsService;
    private final HearingBookingService hearingBookingService;
    private final NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        if (eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class).isEmpty()) {
            hearingBookingService.getFirstHearing(caseData.getHearingDetails())
                .ifPresent(hearingBooking ->
                    caseDetails.getData().put("proceedingLabel", buildProceedingLabel(hearingBooking)));
        }

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            String assignedJudgeLabel = buildAllocatedJudgeLabel(caseData.getAllocatedJudge());

            caseDetails.getData().put("noticeOfProceedings", ImmutableMap.of(
                "judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder()
                    .allocatedJudgeLabel(assignedJudgeLabel)
                    .build()
            ));
        }

        return respond(caseDetails, eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class));
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge());

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        NoticeOfProceedings noticeOfProceedings = caseData.getNoticeOfProceedings();
        noticeOfProceedings = noticeOfProceedings.toBuilder().judgeAndLegalAdvisor(judgeAndLegalAdvisor).build();

        caseDetails.getData().put("noticeOfProceedings", noticeOfProceedings);
        caseData = getCaseData(caseDetails);

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);

        List<DocmosisTemplates> templateTypes = getProceedingTemplateTypes(caseData);

        List<Document> uploadedDocuments = generateAndUploadDocuments(templateData, templateTypes);

        List<Element<DocumentBundle>> noticeOfProceedingCaseData = createNoticeOfProceedingsCaseData(uploadedDocuments);

        if (isNotEmpty(callbackRequest.getCaseDetailsBefore().getData().get("noticeOfProceedingsBundle"))) {
            CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

            noticeOfProceedingCaseData.addAll(noticeOfProceedingsService
                .getRemovedDocumentBundles(caseDataBefore, templateTypes));
        }

        caseDetails.getData().put("noticeOfProceedingsBundle", noticeOfProceedingCaseData);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        if (featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)) {
            publishEvent(new NoticeOfProceedingsIssuedEvent(getCaseData(callbackRequest)));
        }
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

    private List<Document> generateAndUploadDocuments(DocmosisNoticeOfProceeding templatePlaceholders,
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
