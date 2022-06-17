package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsRefusedFormatter {

    public String getRespondentsRefusedLabel(CaseData caseData) {
        return RespondentsCommonFormatHelper.getRespondentsLabel(caseData);
    }

    public String getRespondentsNamesForDocument(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        List<String> selected = RespondentsCommonFormatHelper
            .getSelectedARespondents(caseData, caseData.getRespondentsRefusedSelector());

        selected.forEach(builder::append);

        return builder.toString();
    }

    public String getRespondentsNamesForTab(CaseData caseData) {
        return RespondentsCommonFormatHelper.getRespondentsForTab(caseData, caseData.getRespondentsRefusedSelector());
    }
}
