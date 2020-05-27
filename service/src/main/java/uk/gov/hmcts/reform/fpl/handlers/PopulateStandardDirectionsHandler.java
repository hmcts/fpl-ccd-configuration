package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PopulateStandardDirectionsHandler {
    private static final Boolean IGNORE_WARNING = true;
    private final ObjectMapper mapper;
    private final StandardDirectionsService standardDirectionsService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final SystemUpdateUserConfiguration userConfig;
    private final CommonDirectionService commonDirectionService;
    private final HearingBookingService hearingBookingService;

    @Async
    @EventListener
    public void populateStandardDirections(PopulateStandardDirectionsEvent event) throws IOException {
        String userToken = idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
        String systemUpdateUserId = idamClient.getUserInfo(userToken).getUid();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            event.getCallbackRequest().getCaseDetails().getJurisdiction(),
            event.getCallbackRequest().getCaseDetails().getCaseTypeId(),
            event.getCallbackRequest().getCaseDetails().getId().toString(),
            "populateSDO");

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                .id(startEventResponse.getEventId())
                .build())
            .data(populateStandardDirections(event.getCallbackRequest()))
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            event.getCallbackRequest().getCaseDetails().getJurisdiction(),
            event.getCallbackRequest().getCaseDetails().getCaseTypeId(),
            event.getCallbackRequest().getCaseDetails().getId().toString(),
            IGNORE_WARNING,
            caseDataContent);
    }

    private Map<String, Object> populateStandardDirections(CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        HearingBooking firstHearing = getFirstHearing(caseData.getHearingDetails());

        List<Element<Direction>> directions = standardDirectionsService.getDirections(firstHearing);

        commonDirectionService.sortDirectionsByAssignee(directions)
            .forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));

        return caseDetails.getData();
    }

    private HearingBooking getFirstHearing(List<Element<HearingBooking>> hearings) {
        return hearingBookingService.getFirstHearing(hearings).orElse(null);
    }
}
