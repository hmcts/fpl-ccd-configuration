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
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewNoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;
    private final EmailNotificationHelper emailNotificationHelper;

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(CaseDetails caseDetails) {
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        List<Element<HearingBooking>> hearings = hearingBookingService.getSelectedHearings(caseData);

        String subject = emailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix(caseData, caseData.getHearingDetails());

        return NewNoticeOfHearingTemplate.builder()
            .caseUrl(getCaseUrl(caseDetails.getId()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .localAuthority(caseData.getCaseLocalAuthority())
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }

    private List<String> buildNewHearingBookings(List<Element<HearingBooking>> hearingBookingsList) {
        ImmutableList.Builder<String> newHearingBookingsBuilder = ImmutableList.builder();

        hearingBookingsList
            .forEach(element -> newHearingBookingsBuilder.add(String.valueOf(element)));
        return newHearingBookingsBuilder.build();
    }
}
