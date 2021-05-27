package uk.gov.hmcts.reform.fpl.service.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HearingService {

    private final Time time;

    public final Optional<Element<HearingBooking>> findHearing(CaseData caseData, DynamicList fieldSelector) {

        if (fieldSelector == null) {
            return Optional.empty();
        }

        UUID selectedHearingCode = fieldSelector.getValueCodeAsUUID();

        if (selectedHearingCode == null) {
            return Optional.empty();
        }

        return caseData.findHearingBookingElement(selectedHearingCode);
    }

    public List<Element<HearingBooking>> findOnlyHearingsTodayOrInPastNonVacated(CaseData caseData) {

        List<Element<HearingBooking>> activeHearings = nullSafeList(caseData.getHearingDetails());
        List<Element<HearingBooking>> cancelledHearings = nullSafeList(caseData.getCancelledHearingDetails());

        return Stream.concat(activeHearings.stream(), cancelledHearings.stream())
            .filter(it -> !it.getValue().isVacated())
            .filter(it -> it.getValue().getEndDate().compareTo(time.now()) <= 0)
            .collect(toList());
    }


}
