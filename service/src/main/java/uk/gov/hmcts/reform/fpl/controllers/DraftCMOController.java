package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.CaseManagementOrderService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.Event.DRAFT_CASE_MANAGEMENT_ORDER;

/**
 * Manages the endpoints for the old draft-cmo event. To be removed once we have fully migrated away from the old CMO
 * stuff.
 *
 * @deprecated to be replaced in FPLA-1915
 */
@Api
@RestController
@RequestMapping("/callback/draft-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Deprecated(since = "FPLA-1915")
@SuppressWarnings("java:S1133") // Remove once deprecations dealt with
public class DraftCMOController {
    private final ObjectMapper mapper;
    private final CaseManagementOrderService caseManagementOrderService;
    private final RespondentService respondentService;
    private final OthersService othersService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();
        caseManagementOrderService.prepareCustomDirections(caseDetails, caseManagementOrder);

        if (caseManagementOrder != null) {
            caseDetails.getData().putAll(caseManagementOrder.getCCDFields());
        }

        DynamicList hearingList = caseManagementOrderService.getHearingDateDynamicList(caseData, caseManagementOrder);

        caseDetails.getData().put(HEARING_DATE_LIST.getKey(), hearingList);
        caseDetails.getData().put("respondents_label", getRespondentsLabel(caseData));
        caseDetails.getData().put("others_label", getOthersLabel(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        CaseManagementOrder caseManagementOrder = caseData.getCaseManagementOrder();

        Document document = caseManagementOrderService.getOrderDocument(caseData);
        caseManagementOrder.setOrderDocReferenceFromDocument(document);

        data.put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), caseManagementOrder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseManagementOrderService.removeTransientObjectsFromCaseData(caseDetails.getData());

        caseDetails.getData().put(CASE_MANAGEMENT_ORDER_LOCAL_AUTHORITY.getKey(), caseData.getCaseManagementOrder());
        caseDetails.getData().put("cmoEventId", DRAFT_CASE_MANAGEMENT_ORDER.getId());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    //TODO: logic for only calling this when necessary. When status change new vs old.
    // When new document to share.
    // When no data before.
    // FPLA-1471
    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change-CMO_PROGRESSION"
        );
    }

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthers(), Others.builder().build()));
    }
}
