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
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Api
@RestController
@RequestMapping("/callback/close-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CloseCaseController {

    private final ValidateGroupService validatorService;
    private final ObjectMapper mapper;

    private static final String LABEL = "The case will remain open for 21 days to allow for appeal.\n\n"
        + "In a closed case, you can still:\n"
        + "   •  add a case note\n"
        + "   •  upload a document\n"
        + "   •  issue a C21 (blank order)\n"
        + "   •  submit a C2 application\n";
    private static final String LABEL_FIELD = "close_case_label";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();

        data.put(LABEL_FIELD, LABEL);

        YesNo maybe = new Random().nextBoolean() ? YesNo.YES : YesNo.NO;

        System.out.println(maybe);

        data.put("closeCaseComp", CloseCase.builder().showFullReason(maybe).build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        System.out.println("CaseClose = " + caseData.getCloseCaseComp());
        List<String> errors = validatorService.validateGroup(caseData.getCloseCaseComp());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(errors)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();

        // TODO: 11/05/2020 Mark children and appropriate flags in the case
        // TODO: 11/05/2020 Check what needs to be displayed in the tabs, what is temporary and what is persistent?

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .errors(List.of("STOP"))
            .build();
    }
}
