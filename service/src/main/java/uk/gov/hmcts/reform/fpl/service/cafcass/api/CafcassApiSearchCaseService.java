package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiSearchCaseService {
    private static final MustNot CASE_STATES = MustNot.builder()
        .clauses(List.of(
            MatchQuery.of("state", "Open"),
            MatchQuery.of("state", "CLOSED"),
            MatchQuery.of("state", "Deleted"),
            MatchQuery.of("state", "RETURNED")))
        .build();

    private final CaseConverter caseConverter;
    private final SearchService searchService;
    private final List<CafcassApiCaseDataConverter> cafcassApiCaseDataConverters;

    public List<CafcassApiCase> searchCaseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        final RangeQuery searchRange = RangeQuery.builder()
            .field("last_modified")
            .greaterThanOrEqual(startDate)
            .lessThanOrEqual(endDate).build();

        final BooleanQuery searchCaseQuery = BooleanQuery.builder()
            .mustNot(CASE_STATES)
            .filter(Filter.builder()
                .rangeQuery(searchRange)
                .build())
            .build();

        List<CaseDetails> caseDetails = searchService.search(searchCaseQuery, 10000, 0);

        return caseDetails.stream()
            .map(this::convertToCafcassApiCase)
            .toList();
    }

    private CafcassApiCase convertToCafcassApiCase(CaseDetails caseDetails) {
        return CafcassApiCase.builder()
            .caseId(caseDetails.getId())
            .jurisdiction(caseDetails.getJurisdiction())
            .state(caseDetails.getState())
            .caseTypeId(caseDetails.getCaseTypeId())
            .createdDate(caseDetails.getCreatedDate())
            .lastModified(caseDetails.getLastModified())
            .caseData(getCafcassApiCaseData(caseConverter.convert(caseDetails)))
            .build();
    }

    private CafcassApiCaseData getCafcassApiCaseData(CaseData caseData) {
        CafcassApiCaseData.CafcassApiCaseDataBuilder builder = CafcassApiCaseData.builder();

        for (CafcassApiCaseDataConverter converter : cafcassApiCaseDataConverters) {
            builder = converter.convert(caseData, builder);
        }

        return builder.build();
    }
}
