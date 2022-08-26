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
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Sort;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.SortOrder;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.SortQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor
@Slf4j
public class CMSReportService {
    private final SearchService searchService;
    private final AuditEventService auditEventService;
    private static final String MATCH_FIELD = "data.court.code";
    private static final String SORT_FIELD = "created_date";


    public String getReport(CaseData caseData) {
        CMSReportEventData cmsReportEventData = caseData.getCmsReportEventData();
        String courtId = getCourt(cmsReportEventData);
        ESQuery esQuery = buildQuery(courtId);
        log.info("query {}", esQuery.toMap());
        int count = searchService.searchResultsSize(esQuery);
        log.info("record count {}", count);

        List<CaseDetails> searchResult = searchService.search(esQuery,
                50,
                1,
                buildSortClause());

        int[] counter = new int[]{1};

        StringBuilder result = new StringBuilder();

        for(CaseDetails caseDetails : searchResult) {
            LocalDate submitApplication = auditEventService.getOldestAuditEventByName(
                            String.valueOf(caseDetails.getId()),
                            "submitApplication")
                    .map(auditEvent -> {
                        log.info("Created datetime: {}", auditEvent.getCreatedDate());
                        return auditEvent.getCreatedDate().toLocalDate();
                    })
                    .orElseGet(LocalDate::now);

            result.append(
                    String.join("",
                            "<div class='panel panel-border-wide'>",
                            String.valueOf(counter[0]++),
                            ".  ",
                            String.valueOf(caseDetails.getId()),
                            " - ",
                            String.valueOf(caseDetails.getState()),
                            " - ",
                            String.valueOf(caseDetails.getData().get("familyManCaseNumber")),
                            " - ",
                            String.valueOf(caseDetails.getData().get("caseLocalAuthority")),
                            " - ",
                            "Submitted on :",
                            formatLocalDateToString(submitApplication, "dd-MM-yyyy"),
                            "</div>")
            );
        }
      /*  search.stream()
                .forEach(caseDetails -> String.join("",
                        "<div class='panel panel-border-wide'>",
                        String.valueOf(counter[0]++),
                        ".  ",
                        String.valueOf(caseDetails.getId()),
                        " - ",
                        String.valueOf(caseDetails.getState()),
                        " - ",
                        String.valueOf(caseDetails.getData().get("familyManCaseNumber")),
                        " - ",
                        String.valueOf(caseDetails.getData().get("caseLocalAuthority")),
                        "</div>"));*/

/*        String result = search.stream()
                .map(caseDetails -> String.join("",
                        "<div class='panel panel-border-wide'>",
                        String.valueOf(counter[0]++),
                        ".  ",
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

                        Object::toString));*/

        return String.join("",
                "Total record count : ",
                String.valueOf(count),
                System.lineSeparator(),
                result);
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

    private Sort buildSortClause() {
        return Sort.builder()
                .clauses(List.of(
                    SortQuery.of(SORT_FIELD, SortOrder.DESC)
                ))
                .build();
    }
}
