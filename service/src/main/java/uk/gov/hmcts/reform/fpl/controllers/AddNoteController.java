package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseNoteService;
import uk.gov.hmcts.reform.fpl.utils.CaseDataConverter;

import java.util.List;

@Api
@RestController
@RequestMapping("/callback/add-note")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddNoteController {
    private final CaseNoteService service;
    private final RequestData requestData;
    private final CaseDataConverter caseDataConverter;

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = caseDataConverter.convertToCaseData(caseDetails);

        CaseNote caseNote = service.buildCaseNote(requestData.authorisation(), caseData.getCaseNote());
        List<Element<CaseNote>> caseNotes = service.addNoteToList(caseNote, caseData.getCaseNotes());

        CaseData updatedCase = caseData.toBuilder()
            .caseNotes(caseNotes)
            .caseNote(null)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataConverter.convertToMap(updatedCase))
            .build();
    }
}
