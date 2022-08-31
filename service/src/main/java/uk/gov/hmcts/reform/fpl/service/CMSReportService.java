package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Hearings;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.CMSReportEventData;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.BooleanQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Filter;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.RangeQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Sort;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.SortOrder;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.SortQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.TermQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.TermsQuery;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Service
@RequiredArgsConstructor
@Slf4j
public class CMSReportService {
    private static final String MATCH_FIELD = "data.court.code";
    private static final String SORT_FIELD = "dateSubmitted";
    private static final String RANGE_FIELD = "dateSubmitted";

    private final SearchService searchService;
    private final AuditEventService auditEventService;
    private final static List<HearingType> REQUIRED_HEARING_TYPE = List.of(
            CASE_MANAGEMENT, ISSUE_RESOLUTION, FINAL
    );
    private final static List<String> REQUIRED_STATES = List.of (
            "submitted","gatekeeping","prepare_for_hearing","final_hearing"
    );

    public String getReport(CaseData caseData) throws JsonProcessingException {
        return getReportCasesAtRisk(caseData, (complianceDeadline) ->  RangeQuery.builder()
                .field(RANGE_FIELD)
                .greaterThanOrEqual(complianceDeadline)
                .build());
    }

    private String getReportCasesAtRisk(CaseData caseData, Function<LocalDate, RangeQuery> rangeQueryFunction) throws JsonProcessingException {
        CMSReportEventData cmsReportEventData = caseData.getCmsReportEventData();
        String courtId = getCourt(cmsReportEventData);
        LocalDate complianceDeadline = LocalDate.now().minusWeeks(26);


        ESQuery esQuery = buildQuery(courtId, rangeQueryFunction.apply(complianceDeadline));
        log.info("query {}", esQuery.toMap());

        SearchResult searchResult = searchService.search(esQuery,
                50,
                0,
                buildSortClause());
        log.info("record count {}", searchResult.getTotal());

        int[] counter = new int[]{1};
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder result = new StringBuilder();

        LocalDate currentDate = LocalDate.now();
        for (CaseDetails caseDetails : searchResult.getCases()) {

            LocalDate dateSubmitted = LocalDate.parse((String) caseDetails.getData().get("dateSubmitted"));
            log.info("hearing details {}", caseDetails.getData().get("hearingDetails"));
            Hearings hearing = objectMapper.readValue((String) caseDetails.getData().get("hearingDetails"), Hearings.class);

            Optional<HearingBooking> lastKnowHearing = hearing.getHearingDetails().stream()
                    .filter(hearingDetail -> REQUIRED_HEARING_TYPE.contains(hearingDetail.getValue().getType()))
                    .map(Element::getValue)
                    .max(Comparator.comparing(HearingBooking::getStartDate));

            if (lastKnowHearing.isPresent()) {
                HearingBooking hearingBooking = lastKnowHearing.get();
                result.append(
                        String.join("",
                                "<div class='panel panel-border-wide'>",
                                String.valueOf(counter[0]++),
                                ".  ",
                                String.valueOf(caseDetails.getData().get("familyManCaseNumber")),
                                " - ",
                                String.valueOf(caseDetails.getData().get("caseLocalAuthority")),
                                " - ",
                                "Last hearing",
                                "-",
                                formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), "dd-MM-yyyy"),
                                "-",
                                hearingBooking.getType().getLabel(),
                                "Age of case in weeks",
                                String.valueOf(ChronoUnit.WEEKS.between(dateSubmitted, currentDate)),
                                "Expected FH date",
                                formatLocalDateToString(dateSubmitted.plusWeeks(26),"dd-MM-yyyy"),
                                "</div>")
                );

            }
        }


        return String.join("",
                "Total record count : ",
                String.valueOf(searchResult.getTotal()),
                System.lineSeparator(),
                result);
    }

    private String getCourt(CMSReportEventData cmsReportEventData) {
        return Optional.ofNullable(cmsReportEventData.getCarlisleDFJCourts())
                .orElseGet(() -> Optional.ofNullable(cmsReportEventData.getCentralLondonDFJCourts())
                        .orElseGet(cmsReportEventData::getSwanseaDFJCourts));

    }

    private ESQuery buildQuery(String courtId, RangeQuery rangeQuery) {
        TermQuery termQuery = TermQuery.of(MATCH_FIELD, courtId);
        TermsQuery termsQuery = TermsQuery.of("state", REQUIRED_STATES);


        Filter filter = Filter.builder()
                .termQuery(termQuery)
                .termsQuery(termsQuery)
                .rangeQuery(rangeQuery)
                .build();

        return BooleanQuery.builder()
                .filter(filter)
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
