package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AppointedGuardianFormatter {

    public String getGuardiansLabel(CaseData caseData) {
        return RespondentsCommonFormatHelper.getRespondentsLabel(caseData);
    }

    public String getGuardiansNamesForDocument(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        List<String> selected = RespondentsCommonFormatHelper
            .getSelectedARespondents(caseData, caseData.getAppointedGuardianSelector());

        selected.forEach(builder::append);
        appendChildGrammarVerb(builder, selected.size() > 1);

        return builder.toString();
    }

    public String getGuardiansNamesForTab(CaseData caseData) {
        return RespondentsCommonFormatHelper.getRespondentsForTab(caseData, caseData.getAppointedGuardianSelector());
    }

    private static void appendChildGrammarVerb(StringBuilder builder, boolean hasMultipleGuardiansGrammar) {
        if (builder.toString().isEmpty()) {
            return;
        }
        if (hasMultipleGuardiansGrammar) {
            builder.append(" are");
        } else {
            builder.append(" is");
        }
    }
}
