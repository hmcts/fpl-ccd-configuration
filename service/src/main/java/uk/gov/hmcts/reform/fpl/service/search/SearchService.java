package uk.gov.hmcts.reform.fpl.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SearchService {

    public static final int MAX_SEARCH_SIZE = 10000;

    private final CoreCaseDataService coreCaseDataService;

    public List<CaseDetails> search(ESQuery query) {
        requireNonNull(query);
        return search(query.toQueryContext().toString());
    }

    public List<CaseDetails> search(ESQuery query, int size) {
        requireNonNull(query);
        return search(query.toQueryContext(size).toString());
    }

    public List<CaseDetails> search(String property, LocalDate day) {
        requireNonNull(property);
        requireNonNull(day);
        return search(dateQuery(property, day));
    }

    private List<CaseDetails> search(String query) {
        log.debug("Searching CCD with query: {}", query);
        return coreCaseDataService.searchCases(CASE_TYPE, query);
    }

    private String dateQuery(String property, LocalDate day) {

        final Map<String, Object> dayRange = of(
            "gte", day.atStartOfDay(),
            "lt", day.plusDays(1).atStartOfDay()
        );

        return new JSONObject(
            of("query", of("range", of(property, dayRange)),"size", 1000))
            .toString();
    }
}
