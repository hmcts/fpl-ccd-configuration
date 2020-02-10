package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Api
@RestController
@RequestMapping("/callback/manage-representatives")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepresentativesController {

    private final ObjectMapper mapper;
    private final RepresentativeService representativeService;
    private final RespondentService respondentService;
    private final OthersService othersService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RequestData requestData;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        caseDetails.getData().put("representatives", representativeService.getDefaultRepresentatives(caseData));
        caseDetails.getData().put("respondents_label", getRespondentsLabel(caseData));
        caseDetails.getData().put("others_label", getOthersLabel(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        final List<String> validationErrors = representativeService.validateRepresentatives(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .errors(validationErrors)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails updatedCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails originalCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseData updatedCaseData = mapper.convertValue(updatedCaseDetails.getData(), CaseData.class);
        CaseData originalCaseData = mapper.convertValue(originalCaseDetails.getData(), CaseData.class);

        representativeService.updateRepresentatives(updatedCaseDetails.getId(), updatedCaseData, originalCaseData);

        updatedCaseDetails.getData().put("representatives", updatedCaseData.getRepresentatives());
        updatedCaseDetails.getData().put("others", updatedCaseData.getOthers());
        updatedCaseDetails.getData().put("respondents1", updatedCaseData.getRespondents1());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = mapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        List<Element<Representative>> representativesBefore = caseDataBefore.getRepresentatives();
        List<Element<Representative>> currentRepresentatives = caseData.getRepresentatives();

        List<Element<Representative>> representativeParties = representativeService
            .getRepresentativePartiesToNotify(currentRepresentatives, representativesBefore);

        applicationEventPublisher.publishEvent(new PartyAddedToCaseEvent(
            callbackRequest, requestData.authorisation(), requestData.userId(), representativeParties));
    }

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthers(), Others.builder().build()));
    }
}
