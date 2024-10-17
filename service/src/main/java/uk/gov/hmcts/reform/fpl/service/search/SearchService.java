package uk.gov.hmcts.reform.fpl.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Sort;

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

    public static final int ES_DEFAULT_SIZE = 50;

    private final CoreCaseDataService coreCaseDataService;

    public int searchResultsSize(ESQuery query) {
        requireNonNull(query);
        return search(query.toQueryContext(1, 0).toString()).getTotal();
    }

    public List<CaseDetails> search(ESQuery query, int size, int from) {
        requireNonNull(query);
        return search(query.toQueryContext(size, from).toString()).getCases();
    }

    public List<CaseDetails> search(ESQuery query, int size, int from, List<String> source) {
        requireNonNull(query);
        return search(query.toQueryContext(size, from, source).toString()).getCases();
    }

    public SearchResult search(ESQuery query, int size, int from, Sort sort) {
        requireNonNull(query);
        log.info("sort query {} ", query.toQueryContext(size, from, sort).toString());
        return search(query.toQueryContext(size, from, sort).toString());
    }

    public List<CaseDetails> search(String property, LocalDate day) {
        requireNonNull(property);
        requireNonNull(day);
        return search(dateQuery(property, day)).getCases();
    }

    private SearchResult search(String query) {
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
