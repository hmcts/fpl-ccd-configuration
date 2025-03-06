package uk.gov.hmcts.reform.fpl.controllers;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;

import java.util.List;

@RestController
@RequestMapping("/callback/notice-of-proceedings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsController extends CallbackController {
    private final ValidateGroupService eventValidationService;
    private final NoticeOfProceedingsService noticeOfProceedingsService;

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
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);

        caseDetails.getData().put("noticeOfProceedings",
            noticeOfProceedingsService.setNoticeOfProceedingJudge(caseData));

        caseData = getCaseData(caseDetails);

        List<DocmosisTemplates> docmosisTemplateTypes = caseData.getNoticeOfProceedings()
            .mapProceedingTypesToDocmosisTemplate();

        List<Element<DocumentBundle>> newNoticeOfProceedings =
            noticeOfProceedingsService.uploadNoticesOfProceedings(caseData, docmosisTemplateTypes);

        caseDetails.getData().put("noticeOfProceedingsBundle",
            noticeOfProceedingsService.prepareNoticeOfProceedingBundle(newNoticeOfProceedings,
                noticeOfProceedingsService.getPreviousNoticeOfProceedings(caseDataBefore), docmosisTemplateTypes));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        //trigger submitted for task creation
    }
}
