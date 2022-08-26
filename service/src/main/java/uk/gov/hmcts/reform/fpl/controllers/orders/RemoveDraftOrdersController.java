package uk.gov.hmcts.reform.fpl.controllers.orders;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrderService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.removeorder.RemoveOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/remove-draft-orders")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RemoveDraftOrdersController extends CallbackController {

    private final DraftOrderService service;
    private final CaseConverter caseConverter;
    private final DocumentListService documentListService;
    private final RemoveOrderService removeOrderService;

    @PostMapping("/about-to-start")
    public CallbackResponse handlePopulateInitialData(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        List<Element<HearingOrder>> allDraftOrders = removeOrderService.getDraftHearingOrders(caseData);

        caseDetailsMap.put("removableOrderList", asDynamicList(allDraftOrders, null, RemovableOrder::asLabel));

        return respond(caseDetailsMap);
    }

    @PostMapping("/mid-event")
    public CallbackResponse handlePopulateDraftInfo(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseDetailsMap caseDetailsMap = CaseDetailsMap.caseDetailsMap(caseDetails);

        return respond(caseDetailsMap);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest request) {
        CaseData caseDataBefore = getCaseDataBefore(request);
        CaseData caseData = getCaseData(request);
    }
}
