package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.HearingCancellationReason;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.HearingVacatedTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class HearingVacatedEmailContentProvider extends AbstractEmailContentProvider {
    public static final String RELIST_ACTION_RELISTED = "be relisted";
    public static final String RELIST_ACTION_NOT_RELISTED = "not be relisted";

    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingVenueLookUpService hearingVenueLookUpService;

    public HearingVacatedTemplate buildHearingVacatedNotification(CaseData caseData,
                                                                  HearingBooking hearingBooking,
                                                                  boolean isRelisted) {
        HearingVenue venue = hearingVenueLookUpService.getHearingVenue(hearingBooking);
        return HearingVacatedTemplate.builder()
            .hearingDate(hearingBooking.getStartDate())
            .hearingDateFormatted(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(),
                    FormatStyle.LONG))
            .hearingVenue(hearingVenueLookUpService.buildHearingVenue(venue))
            .hearingTime(caseDataExtractionService.getHearingTime(hearingBooking))
            .vacatedDate(formatLocalDateToString(hearingBooking.getVacatedDate(), FormatStyle.LONG))
            .vacatedReason(HearingCancellationReason.getHearingCancellationReasonLabel(hearingBooking))
            .relistAction(isRelisted ? RELIST_ACTION_RELISTED : RELIST_ACTION_NOT_RELISTED)
            .familyManCaseNumber(Optional.ofNullable(caseData.getFamilyManCaseNumber())
                .orElse(caseData.getId().toString()))
            .build();
    }
}
