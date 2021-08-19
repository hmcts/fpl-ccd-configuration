package uk.gov.hmcts.reform.fpl.service.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CaseSummaryService {

    private final List<CaseSummaryFieldsGenerator> generators;
    private final ObjectMapper objectMapper;

    @SuppressWarnings("all")
    public CaseSummaryService(
        CaseSummaryCaseFlagGenerator caseSummaryCaseFlagGenerator,
        CaseSummaryOrdersRequestedGenerator caseSummaryOrdersRequestedGenerator,
        CaseSummaryDeadlineGenerator caseSummaryDeadlineGenerator,
        CaseSummaryJudgeInformationGenerator caseSummaryJudgeInformationGenerator,
        CaseSummaryMessagesGenerator caseSummaryMessagesGenerator,
        CaseSummaryNextHearingGenerator caseSummaryNextHearingGenerator,
        CaseSummaryPreviousHearingGenerator caseSummaryPreviousHearingGenerator,
        CaseSummaryFinalHearingGenerator caseSummaryFinalHearingGenerator,
        CaseSummaryPeopleInCaseGenerator caseSummaryPeopleInCaseGenerator,
        CaseSummaryCourtGenerator caseSummaryCourtGenerator,
        ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.generators = List.of(
            caseSummaryCaseFlagGenerator,
            caseSummaryOrdersRequestedGenerator,
            caseSummaryDeadlineGenerator,
            caseSummaryCourtGenerator,
            caseSummaryJudgeInformationGenerator,
            caseSummaryMessagesGenerator,
            caseSummaryNextHearingGenerator,
            caseSummaryPreviousHearingGenerator,
            caseSummaryFinalHearingGenerator,
            caseSummaryPeopleInCaseGenerator
        );
    }

    public Map<String, Object> generateSummaryFields(CaseData caseData) {
        return generators.stream()
            .map(generator -> generator.generate(caseData))
            .flatMap(summary -> objectMapper.convertValue(summary,
                new TypeReference<Map<String, Object>>() {})
                .entrySet().stream())
            .collect(HashMap::new, (m, v) -> {
                Object value = m.getOrDefault(v.getKey(), null);
                if (Objects.isNull(value)) {
                    m.put(v.getKey(), v.getValue());
                }
            }, HashMap::putAll);

    }
}
