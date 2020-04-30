package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.service.PopulateCaseService;

import java.util.List;
import java.util.Map;


@Api
@RestController
@RequestMapping("/callback/populate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateCaseController {
    private final PopulateCaseService populateCaseService;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> data = callbackRequest.getCaseDetails().getData();
        String filename = data.get("caseDataFilename").toString();

        try {
            data.putAll(populateCaseService.getFileData(filename));
        } catch (IllegalArgumentException | JsonProcessingException e) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .errors(List.of(String.format("Could not read file %s", filename)))
                .build();
        }

        data.putAll(populateCaseService.getTimeBasedAndDocumentData());
        data.put("state", populateCaseService.getNewState(filename).getValue());
        if (filename.equals("standardDirectionOrder")) {
            data.put("standardDirectionOrder", populateCaseService.getUpdatedSDOData(data));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
