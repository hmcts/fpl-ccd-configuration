package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.CMSReportEventData;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MatchQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CMSReportService {
    private final SearchService searchService;
    private static final String MATCH_FIELD = "data.court.code";
    //private static final String MATCH_FIELD = "data.court";

    public String getReport(CaseData caseData) {
        CMSReportEventData cmsReportEventData = caseData.getCmsReportEventData();
        String courtId = getCourt(cmsReportEventData);
        ESQuery esQuery = buildQuery(courtId);
        log.info("query ", esQuery);
        log.info("query {}", esQuery.toString());
        List<CaseDetails> search = searchService.search(esQuery, 0, 4);
        log.info("response from ES {} ", search);
        return "court selected " +courtId;
    }

    private String getCourt(CMSReportEventData cmsReportEventData) {
        return Optional.ofNullable(cmsReportEventData.getCarlisleDFJCourts())
                .orElseGet(() -> Optional.ofNullable(cmsReportEventData.getCentralLondonDFJCourts())
                        .orElseGet(cmsReportEventData::getSwanseaDFJCourts));

    }

    private ESQuery buildQuery(String courtId) {
        final String field = "state";
        final MatchQuery openCases = MatchQuery.of(field, State.OPEN.getValue());
        final MatchQuery deletedCases = MatchQuery.of(field, State.DELETED.getValue());
        final MatchQuery returnedCases = MatchQuery.of(field, State.RETURNED.getValue());
        final MatchQuery closedCases = MatchQuery.of(field, State.CLOSED.getValue());

        MustNot mustNot = MustNot.builder()
                .clauses(List.of(openCases, deletedCases, returnedCases, closedCases))
                .build();

        return BooleanQuery.builder()
                .mustNot(mustNot)
                .build();

       /* Must must = Must.builder()
                    .clauses(List.of(
                        MatchQuery.of(MATCH_FIELD, courtId)
                    ))
                    .build();


        return BooleanQuery.builder()
                .must(must)
                .mustNot(mustNot)
                .build();*/
    }
}
