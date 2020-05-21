package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

import java.time.format.FormatStyle;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfProceedingsEmailContentProvider extends AbstractEmailContentProvider {
    private final ObjectMapper mapper;
    private final HearingBookingService hearingBookingService;

    public Map<String, Object> buildAllocatedJudgeNotification(CaseDetails caseDetails) {
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        System.out.println(caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor());

        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()))
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", getCaseUrl(caseDetails.getId()))
            .put("judgeTitle", caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeOrMagistrateTitle())
            .put("judgeName", caseData.getNoticeOfProceedings().getJudgeAndLegalAdvisor().getJudgeName())
            .build();
    }

    private String getHearingBooking(CaseData data) {
        return hearingBookingService.getFirstHearing(data.getHearingDetails())
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
