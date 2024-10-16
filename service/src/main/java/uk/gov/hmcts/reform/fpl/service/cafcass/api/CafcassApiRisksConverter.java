package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiRisk;

import java.util.List;

@Service
public class CafcassApiRisksConverter implements CafcassApiCaseDataConverter {
    private static final CafcassApiRisk EMPTY = CafcassApiRisk.builder().build();
    private static final List<String> SOURCE = List.of("data.risks");

    @Override
    public List<String> getEsSearchSources() {
        return SOURCE;
    }

    @Override
    public CafcassApiCaseData.CafcassApiCaseDataBuilder convert(CaseData caseData,
                                                                CafcassApiCaseData.CafcassApiCaseDataBuilder builder) {
        return builder.risks(getCafcassApiRisk(caseData));
    }

    private CafcassApiRisk getCafcassApiRisk(CaseData caseData) {
        CafcassApiRisk.CafcassApiRiskBuilder builder = CafcassApiRisk.builder();

        Risks risk = caseData.getRisks();
        if (risk != null) {
            builder = builder
                .neglectOccurrences(risk.getNeglectOccurrences())
                .sexualAbuseOccurrences(risk.getSexualAbuseOccurrences())
                .physicalHarmOccurrences(risk.getPhysicalHarmOccurrences())
                .emotionalHarmOccurrences(risk.getEmotionalHarmOccurrences());
        }

        CafcassApiRisk cafcassApiRisk = builder.build();
        return EMPTY.equals(cafcassApiRisk) ? null : cafcassApiRisk;
    }
}
