package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewNoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(
        CaseDetails caseDetails,
        HearingBooking hearingBooking,
        RepresentativeServingPreferences representativeServingPreferences) {
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return getNewNoticeOfHearingTemplateBuilder(caseDetails, hearingBooking, caseData)
            .digitalPreference(representativeServingPreferences == DIGITAL_SERVICE ? "Yes" : "No")
            .caseUrl(representativeServingPreferences == DIGITAL_SERVICE ? getCaseUrl(caseDetails.getId()) : "")
            .build();
    }

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(
        CaseDetails caseDetails,
        HearingBooking hearingBooking) {
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return getNewNoticeOfHearingTemplateBuilder(caseDetails, hearingBooking, caseData)
            .digitalPreference("No")
            .caseUrl("")
            .build();
    }

    private NewNoticeOfHearingTemplate.NewNoticeOfHearingTemplateBuilder getNewNoticeOfHearingTemplateBuilder(CaseDetails caseDetails, HearingBooking hearingBooking, CaseData caseData) {
        return NewNoticeOfHearingTemplate.builder()
            .hearingType(hearingBooking.getType().getLabel().toLowerCase())
            .hearingDate(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG))
            .hearingVenue(hearingVenueLookUpService.getHearingVenue(hearingBooking).getVenue())
            .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
            .preHearingTime(caseDataExtractionService.extractPrehearingAttendance(hearingBooking))
            .caseUrl(getCaseUrl(caseDetails.getId()))
            .documentLink(linkToAttachedDocument(hearingBooking.getNoticeOfHearing()))
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
    }
}
