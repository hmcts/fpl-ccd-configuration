package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.translations.TranslatableItemService;

@RestController
@RequestMapping("/callback/upload-translations")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadTranslationsController extends CallbackController {

    private final TranslatableItemService translatableItemService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();

        caseDetails.getData().put("uploadTranslationsRelatedToDocument",
            translatableItemService.generateList(getCaseData(caseDetails))
        );

        return respond(caseDetails);
    }

    @PostMapping("/select-document/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleValidateMidEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("uploadTranslationsOriginalDoc", translatableItemService.getSelectedOrder(caseData));


        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(translatableItemService.finalise(caseData));

        caseData.getUploadTranslationsEventData()
            .getTransientFields()
            .forEach(field -> caseDetails.getData().remove(field));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        Element<? extends TranslatableItem> lastTranslatedItem =
            translatableItemService.getLastTranslatedItem(getCaseData(callbackRequest));

        TranslatableItem translatableItem = lastTranslatedItem.getValue();

        publishEvent(TranslationUploadedEvent.builder()
            .caseData(getCaseData(callbackRequest))
            .originalDocument(translatableItem.getDocument())
            .amendedDocument(translatableItem.getTranslatedDocument())
            .amendedOrderType(translatableItem.getModifiedItemType())
            .selectedOthers(translatableItem.getSelectedOthers())
            .translationRequirements(translatableItem.getTranslationRequirements())
            .build()
        );
    }

}
