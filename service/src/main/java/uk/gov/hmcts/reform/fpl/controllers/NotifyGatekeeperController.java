package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.NotifyGatekeeperEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Api
@RestController
@RequestMapping("/callback/notify-gatekeeper")
public class NotifyGatekeeperController {
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrdersLookupService ordersLookupService;

    @Autowired
    public NotifyGatekeeperController(ObjectMapper mapper,
                                      ApplicationEventPublisher applicationEventPublisher,
                                      OrdersLookupService ordersLookupService) {
        this.mapper = mapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.ordersLookupService = ordersLookupService;
    }

    private String booleanToYesOrNo(boolean value) {
        return value ? "Yes" : "No";
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackrequest) throws IOException {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        OrderDefinition standardDirectionOrder = ordersLookupService.getStandardDirectionOrder();

        Map<String, List<Element<Direction>>> directions = standardDirectionOrder.getDirections()
            .stream()
            .map(direction -> {
                LocalDateTime completeBy = null;

                if (direction.getDisplay().getDelta() != null && caseData.getHearingDetails() != null) {
                    List<HearingBooking> booking = caseData.getHearingDetails().stream()
                        .map(Element::getValue)
                        .collect(toList());

                    completeBy = buildDateTime(
                        booking.get(0).getDate(), Integer.parseInt(direction.getDisplay().getDelta()));
                }

                return Element.<Direction>builder()
                    .id(randomUUID())
                    .value(Direction.builder()
                        .type(direction.getTitle())
                        .text(direction.getText())
                        .assignee(direction.getAssignee())
                        .directionRemovable(booleanToYesOrNo(direction.getDisplay().isDirectionRemovable()))
                        .readOnly(booleanToYesOrNo(direction.getDisplay().isShowDateOnly()))
                        .completeBy(completeBy)
                        .build())
                    .build();
            })
            .collect(groupingBy(element -> element.getValue().getAssignee().getValue()));

        directions.forEach((key, value) -> caseDetails.getData().put(key, value));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestHeader(value = "authorization") String authorization,
        @RequestHeader(value = "user-id") String userId,
        @RequestBody CallbackRequest callbackRequest) {

        applicationEventPublisher.publishEvent(new NotifyGatekeeperEvent(callbackRequest, authorization, userId));
    }

    private LocalDateTime buildDateTime(LocalDate date, int delta) {
        return date.plusDays(delta).atStartOfDay();
    }
}
