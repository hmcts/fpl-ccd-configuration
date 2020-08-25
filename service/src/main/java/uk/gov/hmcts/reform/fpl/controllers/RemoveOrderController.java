package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.RemoveOrderService;

@Api
@RestController
@RequestMapping("/callback/remove-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoveOrderController {
    private final ObjectMapper mapper;
    private final RemoveOrderService removeOrderService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        CaseData updateCaseData = caseData.toBuilder()
            .activeOrderCollection(removeOrderService.getDropDownListOfExistingOrders(caseData))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(mapper.convertValue(updateCaseData, new TypeReference<>() {
            }))
            .build();
    }
}
