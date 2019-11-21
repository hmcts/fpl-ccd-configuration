package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
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
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.DirectionHelperService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
public class PopulateStandardDirectionsHandler {
    private static final Boolean IGNORE_WARNING = true;
    private final ObjectMapper mapper;
    private final OrdersLookupService ordersLookupService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final SystemUpdateUserConfiguration userConfig;
    private final DirectionHelperService directionHelperService;
    private final HearingBookingService hearingBookingService;

    @Autowired
    public PopulateStandardDirectionsHandler(ObjectMapper mapper,
                                             OrdersLookupService ordersLookupService,
                                             CoreCaseDataApi coreCaseDataApi,
                                             AuthTokenGenerator authTokenGenerator,
                                             IdamClient idamClient,
                                             SystemUpdateUserConfiguration userConfig,
                                             DirectionHelperService directionHelperService,
                                             HearingBookingService hearingBookingService) {
        this.mapper = mapper;
        this.ordersLookupService = ordersLookupService;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
        this.userConfig = userConfig;
        this.directionHelperService = directionHelperService;
        this.hearingBookingService = hearingBookingService;
    }

    @Async
    @EventListener
    public void populateStandardDirections(PopulateStandardDirectionsEvent event) throws IOException {
        String userToken = idamClient.authenticateUser(userConfig.getUserName(), userConfig.getPassword());
        String systemUpdateUserId = idamClient.getUserDetails(userToken).getId();

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

    private Map<String, Object> populateStandardDirections(
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

        List<Element<Direction>> directions = standardDirectionOrder.getDirections()
            .stream()
            .map(direction -> directionHelperService.constructDirectionForCCD(
                direction, getCompleteByDate(caseData, direction)))
            .collect(toList());

        directionHelperService.sortDirectionsByAssignee(directions)
            .forEach((key, value) -> caseDetails.getData().put(key, value));

        return caseDetails.getData();
    }

    private LocalDateTime getCompleteByDate(CaseData caseData, DirectionConfiguration direction) {
        LocalDateTime completeBy = null;

        if (direction.getDisplay().getDelta() != null && caseData.getHearingDetails() != null) {
            HearingBooking mostUrgentBooking = hearingBookingService.getMostUrgentHearingBooking(caseData
                .getHearingDetails());

            completeBy = buildDateTime(mostUrgentBooking.getStartDate(),
                Integer.parseInt(direction.getDisplay().getDelta()));
        }
        return completeBy;
    }

    private LocalDateTime buildDateTime(LocalDateTime date, int delta) {
        return date.plusDays(delta);
    }
}
