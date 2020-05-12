package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingBookingService {
    public static final String HEARING_DETAILS_KEY = "hearingDetails";

    private final Time time;

    public List<Element<HearingBooking>> expandHearingBookingCollection(CaseData caseData) {
        return ofNullable(caseData.getHearingDetails())
            .orElse(newArrayList(element(HearingBooking.builder().build())));
    }

    public List<Element<HearingBooking>> getPastHearings(List<Element<HearingBooking>> hearingDetails) {
        return hearingDetails.stream().filter(this::isPastHearing).collect(toList());
    }

    public HearingBooking getMostUrgentHearingBooking(List<Element<HearingBooking>> hearingDetails) {
        return unwrapElements(hearingDetails).stream()
            .filter(hearing -> hearing.getStartDate().isAfter(time.now()))
            .min(comparing(HearingBooking::getStartDate))
            .orElseThrow(() -> new IllegalStateException("Expected to have at least one hearing booking"));
    }

    public Optional<HearingBooking> getFirstHearing(List<Element<HearingBooking>> hearingDetails) {
        return unwrapElements(hearingDetails).stream()
            .min(comparing(HearingBooking::getStartDate));
    }

    public HearingBooking getHearingBookingByUUID(List<Element<HearingBooking>> hearingDetails, UUID elementId) {
        return hearingDetails.stream()
            .filter(hearingBookingElement -> hearingBookingElement.getId().equals(elementId))
            .map(Element::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * Combines two lists of hearings into one, ordered by start date.
     * Implemented due to work around with hearing start date validation.
     *
     * @param firstList  the first list of hearing bookings to combine.
     * @param secondList the second list of hearing bookings to combine.
     * @return an ordered list of hearing bookings.
     */
    public List<Element<HearingBooking>> combineHearingDetails(List<Element<HearingBooking>> firstList,
                                                               List<Element<HearingBooking>> secondList) {
        List<Element<HearingBooking>> combinedHearingDetails = newArrayList();
        combinedHearingDetails.addAll(firstList);
        combinedHearingDetails.addAll(secondList);

        combinedHearingDetails.sort(comparing(element -> element.getValue().getStartDate()));

        return combinedHearingDetails;
    }

    public List<Element<HearingBooking>> setHearingJudge(List<Element<HearingBooking>> hearingBookings,
                                                         Judge allocatedJudge) {
        return hearingBookings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();

                JudgeAndLegalAdvisor selectedJudge =
                    getSelectedJudge(hearingBooking.getJudgeAndLegalAdvisor(), allocatedJudge);

                removeAllocatedJudgeProperties(selectedJudge);

                hearingBooking.setJudgeAndLegalAdvisor(selectedJudge);

                return buildHearingBookingElement(element.getId(), hearingBooking);
            }).collect(toList());
    }

    public List<Element<HearingBooking>> resetHearingJudge(List<Element<HearingBooking>> hearingBookings,
                                                           Judge allocatedJudge) {
        return hearingBookings.stream()
            .map(element -> {
                HearingBooking hearingBooking = element.getValue();
                JudgeAndLegalAdvisor judgeAndLegalAdvisor = hearingBooking.getJudgeAndLegalAdvisor();

                if (isNotEmpty(judgeAndLegalAdvisor)
                    && allocatedJudge.equalsJudgeAndLegalAdvisor(judgeAndLegalAdvisor)) {
                    judgeAndLegalAdvisor = judgeAndLegalAdvisor.resetJudgeProperties(YES);

                    hearingBooking.setJudgeAndLegalAdvisor(judgeAndLegalAdvisor);
                    return buildHearingBookingElement(element.getId(), hearingBooking);
                }

                return element;
            }).collect(toList());
    }

    private boolean isPastHearing(Element<HearingBooking> element) {
        return ofNullable(element.getValue())
            .map(HearingBooking::getStartDate)
            .filter(hearingDate -> hearingDate.isBefore(time.now()))
            .isPresent();
    }

    private Element<HearingBooking> buildHearingBookingElement(UUID id, HearingBooking hearingBooking) {
        return Element.<HearingBooking>builder()
            .id(id)
            .value(hearingBooking)
            .build();
    }
}
