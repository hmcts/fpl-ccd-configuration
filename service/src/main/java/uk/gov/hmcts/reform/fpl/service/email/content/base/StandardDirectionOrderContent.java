package uk.gov.hmcts.reform.fpl.service.email.content.base;

import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.format.FormatStyle;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.PeopleInCaseHelper.getFirstRespondentLastName;

public abstract class StandardDirectionOrderContent extends AbstractEmailContentProvider {

    protected String getFamilyManCaseNumber(CaseData caseData) {
        return isNull(caseData.getFamilyManCaseNumber()) ? "" : caseData.getFamilyManCaseNumber() + ",";
    }

    protected String getLeadRespondentsName(CaseData caseData) {
        return getFirstRespondentLastName(caseData.getAllRespondents());
    }

    protected String getHearingDate(CaseData data) {
        return data.getFirstHearing()
            .map(hearing -> formatLocalDateToString(hearing.getStartDate().toLocalDate(), FormatStyle.LONG))
            .orElse("");
    }
}
