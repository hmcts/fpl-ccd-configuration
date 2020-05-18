package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

import java.time.format.FormatStyle;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public abstract class StandardDirectionOrderContent extends AbstractEmailContentProvider {

    @Autowired
    private HearingBookingService hearingBookingService;

    protected ImmutableMap.Builder<String, Object> getSDOPersonalisationBuilder(Long caseId, CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber",
                isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()) + ",")
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseId))
            .put("caseUrl", getCaseUrl(caseId));
    }

    private String getHearingBooking(CaseData data) {
        return hearingBookingService.getFirstHearing(data.getHearingDetails())
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
