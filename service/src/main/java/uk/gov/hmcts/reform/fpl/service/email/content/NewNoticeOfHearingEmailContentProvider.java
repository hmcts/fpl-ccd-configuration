package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewNoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(CaseDetails caseDetails, HearingBooking hearingBooking) {
        final CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        return NewNoticeOfHearingTemplate.builder()
            .hearingType(hearingBooking.getType().getLabel())
            .hearingDate(hearingBooking.getStartDate().toString())
            .hearingVenue(hearingBooking.getVenue())
//            .preHearingTime(hearingBooking)
            .caseUrl(getCaseUrl(caseDetails.getId()))
            .familyManCaseNumber(caseData.getFamilyManCaseNumber())
            .respondentLastName(getFirstRespondentLastName(caseData.getRespondents1()))
            .build();
    }

}
