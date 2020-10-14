package uk.gov.hmcts.reform.fpl.service.email.content.base;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.format.FormatStyle;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.capitalize;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.buildSubjectLineWithHearingBookingDateSuffix;

public abstract class StandardDirectionOrderContent extends AbstractEmailContentProvider {

    protected ImmutableMap.Builder<String, Object> getSDOPersonalisationBuilder(CaseData caseData) {
        return ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber",
                isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",")
            .put("leadRespondentsName", capitalize(caseData.getRespondents1()
                .get(0)
                .getValue()
                .getParty()
                .getLastName()))
            .put("hearingDate", getHearingBooking(caseData))
            .put("reference", String.valueOf(caseData.getId()))
            .put("callout", buildCallout(caseData))
            .put("caseUrl", getCaseUrl(caseData.getId()));
    }

    private String getHearingBooking(CaseData data) {
        return data.getFirstHearing()
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }

    private String buildCallout(final CaseData caseData) {
        return "^" + buildSubjectLineWithHearingBookingDateSuffix(caseData.getFamilyManCaseNumber(),
            caseData.getRespondents1(),
            caseData.getFirstHearing().orElse(null));
    }
}
