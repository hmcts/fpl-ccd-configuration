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
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassApiCaseService {
    private final CaseConverter caseConverter;
    private final SearchService searchService;
    private final List<CafcassApiCaseDataConverter> cafcassApiCaseDataConverters;

    public List<CafcassApiCase> searchCaseByDateRange(LocalDateTime startDate, LocalDateTime endDate) {

        final RangeQuery searchRange = RangeQuery.builder()
            .field("last_modified")
            .greaterThanOrEqual(startDate)
            .lessThanOrEqual(endDate).build();

        List<CaseDetails> caseDetails = searchService.search(searchRange, 10000, 0);

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
