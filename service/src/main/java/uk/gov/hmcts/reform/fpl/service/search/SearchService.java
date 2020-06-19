package uk.gov.hmcts.reform.fpl.service.search;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SearchService {

    private final CoreCaseDataService coreCaseDataService;

    public List<CaseDetails> search(String property, LocalDate day) {
        requireNonNull(property);
        requireNonNull(day);
        return coreCaseDataService.searchCases(CASE_TYPE, dateQuery(property, day));
    }

    private String dateQuery(String property, LocalDate day) {

        final Map<String, Object> dayRange = of(
            "gte", day.atStartOfDay(),
            "lt", day.plusDays(1).atStartOfDay()
        );

        return new JSONObject(of("query", of("range", of(property, dayRange)))).toString();
    }
}
