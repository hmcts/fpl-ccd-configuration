package uk.gov.hmcts.reform.fpl.controllers.cmo;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.Map;

@Api
@RestController
@RequestMapping("upload-cmo")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DraftCaseManagementOrderController {

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();

        // populate the list or past hearing dates

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {
        Map<String, Object> data = request.getCaseDetails().getData();

        // map cmo to hearing
        // add to list

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

}
