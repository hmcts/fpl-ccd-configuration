package uk.gov.hmcts.reform.fpl.service.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class HearingService {

    private final ObjectMapper mapper;
    private final Time time;

    public final Optional<Element<HearingBooking>> findHearing(CaseData caseData, Object fieldSelector) {
        if (fieldSelector == null) {
            return Optional.empty();
        }

        UUID selectedHearingCode = getDynamicListSelectedValue(fieldSelector, mapper);
        Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(selectedHearingCode);
        return hearingBooking;
    }

    public List<Element<HearingBooking>> findOnlyHearingsInPast(CaseData caseData) {
        return nullSafeList(caseData.getHearingDetails()).stream()
            .filter(it -> it.getValue().getEndDate().compareTo(time.now()) <= 0)
            .collect(toList());
    }


}
