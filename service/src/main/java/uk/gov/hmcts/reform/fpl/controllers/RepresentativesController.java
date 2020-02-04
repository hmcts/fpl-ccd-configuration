package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseByEmailEvent;
import uk.gov.hmcts.reform.fpl.events.PartyAddedToCaseThroughDigitalServiceEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.*;

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
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        CaseData caseDataBefore = mapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);

        if(caseDataBefore.getRepresentatives().size() == caseData.getRepresentatives().size())
        {
            System.out.println("None new added");
        } else {
            System.out.println("New added");
            int representativeAdded = caseData.getRepresentatives().size() - 1;
            RepresentativeServingPreferences servingPreferences = caseData.getRepresentatives()
                .get(representativeAdded).getValue().getServingPreferences();

            if(servingPreferences.equals(EMAIL))
            {
                applicationEventPublisher.publishEvent(new PartyAddedToCaseByEmailEvent(callbackRequest, authorization, userId));
            } else if(servingPreferences.equals(DIGITAL_SERVICE)) {
                applicationEventPublisher.publishEvent(new PartyAddedToCaseThroughDigitalServiceEvent(callbackRequest, authorization, userId));
            }
        }
    }

    private String getRespondentsLabel(CaseData caseData) {
        return respondentService.buildRespondentLabel(defaultIfNull(caseData.getRespondents1(), emptyList()));
    }

    private String getOthersLabel(CaseData caseData) {
        return othersService.buildOthersLabel(defaultIfNull(caseData.getOthers(), Others.builder().build()));
    }
}
