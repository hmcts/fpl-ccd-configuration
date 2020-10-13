package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.events.NoticeOfProceedingsIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisNoticeOfProceeding;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsTemplateDataGenerationService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;

@Api
@RestController
@RequestMapping("/callback/notice-of-proceedings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsController extends CallbackController {
    private final ValidateGroupService eventValidationService;
    private final NoticeOfProceedingsService noticeOfProceedingsService;
    private final NoticeOfProceedingsTemplateDataGenerationService noticeOfProceedingsTemplateDataGenerationService;
    private final FeatureToggleService featureToggleService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = eventValidationService.validateGroup(caseData, NoticeOfProceedingsGroup.class);

        if (errors.isEmpty()) {
            caseDetails.getData().putAll(noticeOfProceedingsService.initNoticeOfProceeding(caseData));
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(
        @RequestBody @NotNull CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseData(callbackRequest.getCaseDetailsBefore());

        caseDetails.getData().put("noticeOfProceedings",
            noticeOfProceedingsService.setNoticeOfProceedingJudge(caseData));

        caseData = getCaseData(caseDetails);

        List<DocmosisTemplates> docmosisTemplateTypes = caseData.getNoticeOfProceedings()
            .mapProceedingTypesToDocmosisTemplate();

        DocmosisNoticeOfProceeding templateData = noticeOfProceedingsTemplateDataGenerationService
            .getTemplateData(caseData);

        List<DocmosisDocument> noticeOfProceedingDocuments =
            noticeOfProceedingsService.buildNoticeOfProceedingDocuments(templateData, docmosisTemplateTypes);

        List<Document> documentReferences = noticeOfProceedingsService.uploadDocuments(noticeOfProceedingDocuments);

        List<Element<DocumentBundle>> newNoticeOfProceedings = createNoticeOfProceedingsCaseData(documentReferences);

        caseDetails.getData().put("noticeOfProceedingsBundle",
            noticeOfProceedingsService.prepareNoticeOfProceedingBundle(newNoticeOfProceedings,
                getPreviousNoticeOfProceedings(caseDataBefore), docmosisTemplateTypes));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        if (featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS)) {
            publishEvent(new NoticeOfProceedingsIssuedEvent(getCaseData(callbackRequest)));
        }
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

    private List<Element<DocumentBundle>> getPreviousNoticeOfProceedings(CaseData caseDataBefore) {
        if (caseDataBefore == null || caseDataBefore.getNoticeOfProceedingsBundle() == null) {
            return List.of();
        }

        return caseDataBefore.getNoticeOfProceedingsBundle();
    }
}
