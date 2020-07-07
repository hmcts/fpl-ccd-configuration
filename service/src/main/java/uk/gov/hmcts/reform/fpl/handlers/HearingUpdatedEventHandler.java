package uk.gov.hmcts.reform.fpl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.HearingsUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingUpdatedEventHandler {

    private final HearingBookingService hearingBookingService;
    private final ObjectMapper mapper;

    @Async
    @EventListener
    public void sendEmail(final HearingsUpdated event) {
        final CaseData caseData = mapper.convertValue(event.getCallbackRequest().getCaseDetails().getData(), CaseData.class);
    }


}
