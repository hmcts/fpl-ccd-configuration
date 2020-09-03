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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseController {

    private final ObjectMapper mapper;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        if (caseData.getFamilyManCaseNumber().equals("SA20C50019")) {
            List<Element<Child>> children = caseData.getChildren1();

            for (Element<Child> child : children) {
                child.getValue().setFinalOrderIssued(null);
                child.getValue().setFinalOrderIssuedType(null);
            }

            caseData.getOrderCollection().remove(1);
            data.put("orderCollection", caseData.getOrderCollection());
            data.put("children1", children);
            data.put("state", State.CASE_MANAGEMENT.getValue());
            data.remove("closeCaseTabField");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
