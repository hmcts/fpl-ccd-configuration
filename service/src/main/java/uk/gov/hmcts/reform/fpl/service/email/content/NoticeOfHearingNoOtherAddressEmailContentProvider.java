package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NoticeOfHearingNoOtherAddressTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.HEARINGS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.formatCCDCaseNumber;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfHearingNoOtherAddressEmailContentProvider extends AbstractEmailContentProvider {

    public NoticeOfHearingNoOtherAddressTemplate buildNewNoticeOfHearingNoOtherAddressNotification(CaseData caseData,
                                                                                       HearingBooking hearingBooking,
                                                                                       Other other) {

        return NoticeOfHearingNoOtherAddressTemplate.builder()
            .familyManCaseNumber(defaultIfNull(caseData.getFamilyManCaseNumber(), ""))
            .ccdNumber(formatCCDCaseNumber(caseData.getId()))
            .hearingType(hearingBooking.getType().getLabel().toLowerCase())
            .hearingDate(formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), FormatStyle.LONG))
            .partyName(other.getName())
            .caseUrl(getCaseUrl(caseData.getId(), HEARINGS))
            .build();
    }
}
