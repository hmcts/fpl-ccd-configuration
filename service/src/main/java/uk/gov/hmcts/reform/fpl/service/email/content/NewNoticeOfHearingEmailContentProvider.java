package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewNoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(CaseDetails newCaseDetails, CaseDetails oldCaseDetails) {
        CaseData newCaseData = mapper.convertValue(newCaseDetails.getData(), CaseData.class);
        CaseData oldCaseData = mapper.convertValue(oldCaseDetails.getData(), CaseData.class);

        List<Element<HearingBooking>> listOfHearingBookings = hearingBookingService.getNewHearings(newCaseData.getHearingDetails(), oldCaseData.getHearingDetails());

        return NewNoticeOfHearingTemplate.builder()
            .caseUrl(getCaseUrl(newCaseDetails.getId()))
            .familyManCaseNumber(newCaseData.getFamilyManCaseNumber())
            .hearingDetails(buildNewHearingBookings(listOfHearingBookings))
            .localAuthority(newCaseData.getCaseLocalAuthority())
            .respondentLastName(getFirstRespondentLastName(newCaseData.getRespondents1()))
            .build();
    }

    private List<String> buildNewHearingBookings(List<Element<HearingBooking>> hearingBookingsList) {
        ImmutableList.Builder<String> newHearingBookingsBuilder = ImmutableList.builder();

        hearingBookingsList
            .forEach(element -> newHearingBookingsBuilder.add(String.valueOf(element)));
        return newHearingBookingsBuilder.build();
    }
}
