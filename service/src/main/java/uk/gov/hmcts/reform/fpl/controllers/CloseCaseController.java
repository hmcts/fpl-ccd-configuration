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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.validation.groups.CloseCaseGroup;

import java.util.List;
import java.util.Map;

@Api
@RestController
@RequestMapping("/callback/close-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CloseCaseController {

    private static final String LABEL = "The case will remain open for 21 days to allow for appeal.\n\n"
        + "In a closed case, you can still:\n"
        + "   •  add a case note\n"
        + "   •  upload a document\n"
        + "   •  issue a C21 (blank order)\n"
        + "   •  submit a C2 application\n";
    private static final String LABEL_FIELD = "close_case_label";
    private static final String CLOSE_CASE_FIELD = "closeCase";
    private static final String CLOSE_CASE_TAB_FIELD = "closeCaseTabField";
    private static final String DEPRIVATION_OF_LIBERTY_FLAG = "deprivationOfLiberty";
    private final ValidateGroupService validatorService;
    private final ChildrenService childrenService;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        data.put(LABEL_FIELD, LABEL);

        // TODO: 11/05/2020 Determine YES or NO based on if all children have a final order
        boolean displayFinalOrder = childrenService.allChildrenHaveFinalOrder(caseData.getAllChildren());

        data.put(CLOSE_CASE_FIELD, CloseCase.builder().showFullReason(YesNo.from(displayFinalOrder)).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        List<String> errors = validatorService.validateGroup(caseData.getCloseCase(), CloseCaseGroup.class);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);
        // TODO: 11/05/2020 Mark children
        data.put(DEPRIVATION_OF_LIBERTY_FLAG, YesNo.from(caseData.getCloseCase().hasDeprivationOfLiberty()).getValue());
        data.put(CLOSE_CASE_TAB_FIELD, caseData.getCloseCase());

        data.remove(CLOSE_CASE_FIELD);
        data.remove(LABEL_FIELD);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
