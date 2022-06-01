package uk.gov.hmcts.reform.fpl.service.summary;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

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
        CaseSummaryWelshFlagGenerator caseSummaryWelshFlagGenerator,
        CaseSummaryPeopleInCaseGenerator caseSummaryPeopleInCaseGenerator,
        CaseSummaryCourtGenerator caseSummaryCourtGenerator,
        CaseSummaryHighCourtCaseFlagGenerator caseSummaryHighCourtCaseFlagGenerator,
        ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.generators = List.of(
            caseSummaryCaseFlagGenerator,
            caseSummaryOrdersRequestedGenerator,
            caseSummaryDeadlineGenerator,
            caseSummaryCourtGenerator,
            caseSummaryJudgeInformationGenerator,
            caseSummaryWelshFlagGenerator,
            caseSummaryMessagesGenerator,
            caseSummaryNextHearingGenerator,
            caseSummaryPreviousHearingGenerator,
            caseSummaryFinalHearingGenerator,
            caseSummaryPeopleInCaseGenerator,
            caseSummaryHighCourtCaseFlagGenerator
        );
    }

    public Map<String, Object> generateSummaryFields(CaseData caseData) {
        Map<String, Object> ret = generators.stream()
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
        // X AND (Y OR Z) doesn't work at this moment
        // introduced caseSummaryLATabHidden for an alternative solution.
        // "TabShowCondition": "[STATE] != \"Open\" AND [STATE] != \"RETURNED\"
        // AND (caseSummaryLALanguageRequirement = \"Yes\" OR caseSummaryLAHighCourtCase = \"Yes\")",
        if (ret.containsKey("caseSummaryLAHighCourtCase") && ret.containsKey("caseSummaryLALanguageRequirement")) {
            ret.put("caseSummaryLATabHidden", YES.getValue().equals(ret.get("caseSummaryLAHighCourtCase"))
                || YES.getValue().equals(ret.get("caseSummaryLALanguageRequirement")) ? NO.getValue() : YES.getValue());
        }
        return ret;
    }
}
