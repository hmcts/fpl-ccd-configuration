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
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Must;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.MustNot;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        log.info("query {}", esQuery.toMap());
        log.info("record count {}", searchService.searchResultsSize(esQuery));

        List<CaseDetails> search = searchService.search(esQuery, 10, 1);


        String result = search.stream()
                .map(caseDetails -> String.join("",
                        "<div class='panel panel-border-wide'>",
                        String.valueOf(caseDetails.getId()),
                        " - ",
                        String.valueOf(caseDetails.getState()),
                        " - ",
                        String.valueOf(caseDetails.getData().get("familyManCaseNumber")),
                        " - ",
                        String.valueOf(caseDetails.getData().get("caseLocalAuthority")),
                        "</div>")
                )
                .collect(Collectors.collectingAndThen(Collectors.toSet(),
                        Object::toString));

        log.info("response from ES {} ", search);
        return result;
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


       Must must = Must.builder()
                    .clauses(List.of(
                        MatchQuery.of(MATCH_FIELD, courtId)
                    ))
                    .build();


        return BooleanQuery.builder()
                .must(must)
                .mustNot(mustNot)
                .build();
    }
}
