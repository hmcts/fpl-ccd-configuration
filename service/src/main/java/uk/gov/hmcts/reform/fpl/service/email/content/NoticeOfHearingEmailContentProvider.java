package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.cafcass.NoticeOfHearingCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.format.FormatStyle;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.HEARINGS;
import static uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate.builder;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingVenueLookUpService hearingVenueLookUpService;
    private final EmailNotificationHelper helper;

    public NoticeOfHearingTemplate buildNewNoticeOfHearingNotification(CaseData caseData,
                                                                       HearingBooking hearingBooking,
                                                                       RepresentativeServingPreferences preference) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);
        DocumentReference noticeOfHearing = hearingBooking.getNoticeOfHearing();

        return builder()
            .hearingType(hearingBooking.getType().getLabel().toLowerCase())
            .hearingDate(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
            .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
            .preHearingTime(hearingBooking.getPreAttendanceDetails())
            .documentLink(DIGITAL_SERVICE == preference ? getDocumentUrl(noticeOfHearing)
                : linkToAttachedDocument(noticeOfHearing))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .lastName(getFirstRespondentLastName(caseData))
            .digitalPreference(DIGITAL_SERVICE == preference ? "Yes" : "No")
            .caseUrl(DIGITAL_SERVICE == preference ? getCaseUrl(caseData.getId(), HEARINGS) : "")
            .childLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
            .build();
    }


    public NoticeOfHearingCafcassData buildNewNoticeOfHearingNotificationCafcassData(CaseData caseData,
                                                                                     HearingBooking hearingBooking) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);
        return NoticeOfHearingCafcassData.builder()
                .hearingType(hearingBooking.getType().getLabel().toLowerCase())
                .eldestChildLastName(helper.getEldestChildLastName(caseData.getAllChildren()))
                .firstRespondentName(getFirstRespondentLastName(caseData))
                .hearingDate(hearingBooking.getStartDate())
                .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
                .preHearingTime(hearingBooking.getPreAttendanceDetails())
                .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
                .build();
    }
}
