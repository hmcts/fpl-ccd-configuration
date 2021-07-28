package uk.gov.hmcts.reform.fpl.service.summary;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.summary.SyntheticCaseSummary;
import uk.gov.hmcts.reform.fpl.service.CourtService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CaseSummaryCourtGenerator implements CaseSummaryFieldsGenerator {

    private final CourtService courtService;

    @Override
    public SyntheticCaseSummary generate(CaseData caseData) {
        return SyntheticCaseSummary.builder()
            .caseSummaryCourtName(courtService.getCourtName(caseData))
            .build();
    }
}
