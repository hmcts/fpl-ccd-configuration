package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsEmailContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public AllocatedJudgeTemplateForNoticeOfProceedings buildAllocatedJudgeNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocatedJudgeTemplateForNoticeOfProceedings allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForNoticeOfProceedings();

        allocatedJudgeTemplate.setFamilyManCaseNumber(caseData.getFamilyManCaseNumber());
        allocatedJudgeTemplate.setRespondentLastName(getFirstRespondentLastName(caseData.getRespondents1()));
        allocatedJudgeTemplate.setHearingDate(getHearingBookingStartDate(caseData.getHearingDetails()));
        allocatedJudgeTemplate.setCaseUrl(getCaseUrl(caseDetails.getId()));
        allocatedJudgeTemplate.setJudgeTitle(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()
            .getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeName());

        return allocatedJudgeTemplate;
    }

    private String getHearingBookingStartDate(List<Element<HearingBooking>> hearingDetails) {
        return hearingBookingService.getFirstHearing(hearingDetails)
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
