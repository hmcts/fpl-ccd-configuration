package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFeatureFlag;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Must;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.TermQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.TermsQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
public class CafcassApiSearchCaseService {
    private static final MustNot MUST_NOT = MustNot.builder()
        .clauses(List.of(
            MatchQuery.of("state", "Open"),
            MatchQuery.of("state", "Deleted"),
            MatchQuery.of("state", "RETURNED"),
            TermQuery.of("data.court.regionId", "7")))
        .build();

    private final CaseConverter caseConverter;
    private final SearchService searchService;
    private final List<CafcassApiCaseDataConverter> cafcassApiCaseDataConverters;
    private final FeatureToggleService featureToggleService;
    private final List<String> sources;

    CafcassApiSearchCaseService(@Autowired CaseConverter caseConverter, @Autowired SearchService searchService,
                                @Autowired List<CafcassApiCaseDataConverter> cafcassApiCaseDataConverters,
                                @Autowired FeatureToggleService featureToggleService) {
        this.caseConverter = caseConverter;
        this.searchService = searchService;
        this.cafcassApiCaseDataConverters = cafcassApiCaseDataConverters;
        this.featureToggleService = featureToggleService;

        this.sources = cafcassApiCaseDataConverters.stream()
            .map(CafcassApiCaseDataConverter::getEsSearchSources)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    public List<CafcassApiCase> searchCaseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        CafcassApiFeatureFlag flag = featureToggleService.getCafcassAPIFlag();

        if (flag.isEnableApi()) {
            final RangeQuery searchRange = RangeQuery.builder()
                .field("data.lastGenuineUpdateTime")
                .greaterThanOrEqual(startDate)
                .lessThanOrEqual(endDate).build();

            final BooleanQuery.BooleanQueryBuilder searchCaseQuery = BooleanQuery.builder()
                .mustNot(MUST_NOT)
                .filter(Filter.builder()
                    .clauses(List.of(searchRange))
                    .build());

            if (isNotEmpty(flag.getWhitelist())) {
                searchCaseQuery.must(Must.builder()
                    .clauses(List.of(TermsQuery.of("data.court.code", flag.getWhitelist())))
                    .build());
            }

            List<CaseDetails> caseDetails = searchService.search(searchCaseQuery.build(), 10000, 0, sources);

            return caseDetails.stream()
                .map(caseDetail -> {
                    log.info("Converting [{}]", caseDetail.getId());
                    return convertToCafcassApiCase(caseDetail);
                })
                .toList();
        } else {
            return List.of();
        }
    }

    public CafcassApiCase convertToCafcassApiCase(CaseDetails caseDetails) {
        return CafcassApiCase.builder()
            .id(caseDetails.getId())
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
