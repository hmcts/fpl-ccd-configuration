package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NewNoticeOfHearingEmailContentProvider extends AbstractEmailContentProvider {

    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public NewNoticeOfHearingTemplate buildNewNoticeOfHearingNotification(CaseDetails newCaseDetails, CaseDetails oldCaseDetails) {
        CaseData newCaseData = mapper.convertValue(newCaseDetails.getData(), CaseData.class);
        CaseData oldCaseData = mapper.convertValue(oldCaseDetails.getData(), CaseData.class);

        NewNoticeOfHearingTemplate newNoticeOfHearingTemplate = NewNoticeOfHearingTemplate.builder()
            .caseUrl(getCaseUrl(newCaseDetails.getId()))
            .familyManCaseNumber(newCaseData.getFamilyManCaseNumber())
            // Not yet sure what to do when there are more than 1 new hearing
//            .hearingDate(hearingBookingService.getNewHearings(newCaseData.getHearingDetails(), oldCaseData.getHearingDetails()).)
            .localAuthority(newCaseData.getCaseLocalAuthority())
            .respondentLastName(getFirstRespondentLastName(newCaseData.getRespondents1()))
            .build();

        return newNoticeOfHearingTemplate;
    }
}
