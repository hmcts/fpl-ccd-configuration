package uk.gov.hmcts.reform.fpl.handlers.judicial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.events.judicial.NewHearingJudgeEvent;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.JudicialService;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        if (isEmpty(event.getHearing()) || isEmpty(event.getHearing().getJudgeAndLegalAdvisor())) {
            // no hearing/hearing judge, return
            log.error("No hearing judge to attempt role assignment on");
            return;
        }

        JudgeAndLegalAdvisor hearingJudge = event.getHearing().getJudgeAndLegalAdvisor();

        Optional<HearingBooking> nextHearing = event.getCaseData().getAllNonCancelledHearings()
            .stream()
            .map(Element::getValue)
            .filter(hearing -> hearing.getStartDate().isAfter(event.getHearing().getStartDate()))
            .min(Comparator.comparing(HearingBooking::getStartDate));

        ZonedDateTime possibleEnd = nextHearing.map(hearing -> hearing.getStartDate().atZone(ZoneId.systemDefault()))
            .orElse(null);


        if (!isEmpty(hearingJudge.getJudgeJudicialUser())
            && !isEmpty(hearingJudge.getJudgeJudicialUser().getIdamId())) {

            // have an IDAM ID - use that to grant the role
            judicialService.assignHearingJudge(event.getCaseData().getId(),
                hearingJudge.getJudgeJudicialUser().getIdamId(),
                event.getHearing().getStartDate().atZone(ZoneId.systemDefault()),
                // if there's a hearing after the one added, we're going out of order, so set an end date
                possibleEnd,
                hearingJudge.getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR));
        } else if (!isEmpty(hearingJudge.getJudgeJudicialUser())
            && !isEmpty(hearingJudge.getJudgeJudicialUser().getPersonalCode())) {

            // no IDAM ID, but has personal code, lookup in JRD first
            Optional<JudicialUserProfile> judge = judicialService
                .getJudge(hearingJudge.getJudgeJudicialUser().getPersonalCode());

            judge.ifPresentOrElse(judicialUserProfile ->
                    judicialService.assignHearingJudge(event.getCaseData().getId(), judicialUserProfile.getSidamId(),
                        event.getHearing().getStartDate().atZone(ZoneId.systemDefault()),
                        possibleEnd,
                        hearingJudge.getJudgeTitle().equals(JudgeOrMagistrateTitle.LEGAL_ADVISOR)),
                () -> log.info("Could not lookup in JRD, no auto allocation of hearing judge on case {}",
                    event.getCaseData().getId()));
        } else {
            log.info("No auto allocation of hearing judge on case {}", event.getCaseData().getId());
        }
    }

}
