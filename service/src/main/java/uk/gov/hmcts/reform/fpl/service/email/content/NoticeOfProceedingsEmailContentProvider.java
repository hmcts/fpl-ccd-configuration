package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.AllocatedJudgeTemplateForNoticeOfProceedings;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsEmailContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public AllocatedJudgeTemplateForNoticeOfProceedings buildAllocatedJudgeNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        AllocatedJudgeTemplateForNoticeOfProceedings allocatedJudgeTemplate
            = new AllocatedJudgeTemplateForNoticeOfProceedings();

        allocatedJudgeTemplate.setFamilyManCaseNumber(isNull(caseData.getFamilyManCaseNumber()) ? ""
                : caseData.getFamilyManCaseNumber() + ",");
        allocatedJudgeTemplate.setLeadRespondentsName(capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()));
        allocatedJudgeTemplate.setHearingDate(getHearingBookingStartDate(caseData));
        allocatedJudgeTemplate.setCaseUrl(getCaseUrl(caseDetails.getId()));
        allocatedJudgeTemplate.setJudgeTitle(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor()
            .getJudgeOrMagistrateTitle());
        allocatedJudgeTemplate.setJudgeName(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeName());

        return allocatedJudgeTemplate;
    }

    private String getHearingBookingStartDate(CaseData caseData) {
        return hearingBookingService.getFirstHearing(caseData.getHearingDetails())
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
