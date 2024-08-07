package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.events.judicial.NewHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import java.util.Comparator;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewHearingJudgeEventHandler {

    private final JudicialService judicialService;

    @Async
    @EventListener
    public void handleNewHearingJudge(final NewHearingJudgeEvent event) {

        handleEditedHearing(event);

        if (isEmpty(event.getHearing()) || isEmpty(event.getHearing().getJudgeAndLegalAdvisor())) {
            // no hearing/hearing judge, return
            log.error("No hearing judge to attempt role assignment on");
            return;
        }

        Optional<HearingBooking> nextHearing = event.getCaseData().getAllNonCancelledHearings()
            .stream()
            .map(Element::getValue)
            .filter(hearing -> hearing.getStartDate().isAfter(event.getHearing().getStartDate()))
            .min(Comparator.comparing(HearingBooking::getStartDate));

        judicialService.assignHearingJudge(event.getCaseData().getId(), event.getHearing(), nextHearing);
    }

    private void handleEditedHearing(final NewHearingJudgeEvent event) {
        if (!isEmpty(event.getOldHearing())) {
            // temp var to get around weird sonar scan behaviour
            Optional<HearingBooking> oldHearing = event.getOldHearing();
            if (oldHearing.isPresent() && !oldHearing.get().equals(event.getHearing())) {
                // the hearing being modified was already on the case - cleanup its roles for us to reassign
                judicialService.deleteSpecificHearingRole(event.getCaseData().getId(), oldHearing.get());
            }
        }
    }

}
