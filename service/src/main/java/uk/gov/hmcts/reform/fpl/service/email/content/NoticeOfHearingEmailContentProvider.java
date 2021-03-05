package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.HEARINGS;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public NoticeOfHearingTemplate buildNewNoticeOfHearingNotification(
        CaseData caseData,
        HearingBooking hearingBooking,
        RepresentativeServingPreferences servingPreference) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);

        return NoticeOfHearingTemplate.builder()
            .hearingType(getHearingType(hearingBooking))
            .hearingDate(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
            .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
            .preHearingTime(caseDataExtractionService.extractPrehearingAttendance(hearingBooking))
            .documentLink(servingPreference == DIGITAL_SERVICE ? getDocumentUrl(hearingBooking.getNoticeOfHearing())
                : linkToAttachedDocument(hearingBooking.getNoticeOfHearing()))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .respondentLastName(getFirstRespondentLastName(caseData))
            .digitalPreference(servingPreference == DIGITAL_SERVICE ? "Yes" : "No")
            .caseUrl(servingPreference == DIGITAL_SERVICE ? getCaseUrl(caseData.getId(), HEARINGS) : "")
            .build();
    }

    private String getHearingType(HearingBooking hearingBooking) {
        return hearingBooking.getType() != OTHER ? hearingBooking.getType().getLabel().toLowerCase() :
            hearingBooking.getTypeDetails();
    }
}
