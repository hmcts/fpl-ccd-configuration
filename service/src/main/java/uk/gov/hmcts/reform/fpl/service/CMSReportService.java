package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.CMSReportEventData;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
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
    private static final String SORT_FIELD = "data.dateSubmitted";
    private static final String RANGE_FIELD = "data.dateSubmitted";

    private final SearchService searchService;
    private final AuditEventService auditEventService;
    private final CaseConverter converter;
    private final static List<HearingType> REQUIRED_HEARING_TYPE = List.of(
            CASE_MANAGEMENT, ISSUE_RESOLUTION, FINAL
    );
    private final static List<String> REQUIRED_STATES = List.of (
            "submitted","gatekeeping","prepare_for_hearing","final_hearing"
    );

    public String getReport(CaseData caseData)  {
        try {
            return getReportCasesAtRisk(caseData, (complianceDeadline) ->  RangeQuery.builder()
                    .field(RANGE_FIELD)
                    .greaterThanOrEqual(complianceDeadline)
                    .build());
        } catch (JsonProcessingException e) {
            log.error("Exception e", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getReportCasesAtRisk(CaseData caseDataSelected, Function<LocalDate, RangeQuery> rangeQueryFunction) throws JsonProcessingException {
        CMSReportEventData cmsReportEventData = caseDataSelected.getCmsReportEventData();
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

        StringBuilder result = new StringBuilder();
        if (searchResult.getTotal() > 0) {
            result.append("<table>")
                    .append("<tr>")
                    .append("<th class='search-result-column-label'>")
                    .append("Case Number")
                    .append("</th>")
                    .append("<th class='search-result-column-label'>")
                    .append("Receipt date")
                    .append("</th>")
                    .append("<th class='search-result-column-label'>")
                    .append("Last hearing")
                    .append("</th>")
                    .append("<th class='search-result-column-label'>")
                    .append("Age of </br>case</br>(weeks)")
                    .append("</th>")
                    .append("<th class='search-result-column-label'>")
                    .append("PLO stage")
                    .append("</th>")
                    .append("<th class='search-result-column-label'>")
                    .append("Expected FH date")
                    .append("</th>")
                    .append("</tr>");
        }

        LocalDate currentDate = LocalDate.now();
        for (CaseDetails caseDetails : searchResult.getCases()) {
            CaseData caseData = converter.convert(caseDetails);

            LocalDate dateSubmitted = caseData.getDateSubmitted();

            List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();

            Optional<HearingBooking> lastKnowHearing = ElementUtils.nullSafeCollection(hearingDetails).stream()
                    .filter(hearingDetail -> REQUIRED_HEARING_TYPE.contains(hearingDetail.getValue().getType()))
                    .map(Element::getValue)
                    .max(Comparator.comparing(HearingBooking::getStartDate));

            String lastHearingDate = "";
            String hearingType = "";
            if (lastKnowHearing.isPresent()) {
                HearingBooking hearingBooking = lastKnowHearing.get();
                lastHearingDate = formatLocalDateToString(hearingBooking.getStartDate().toLocalDate(), "dd-MM-yyyy");
                hearingType = hearingBooking.getType().getLabel();
            }


            result.append(
                String.join("</td><td class='search-result-column-cell'>",
                    "<tr><td class='search-result-column-cell'>",
                    String.valueOf(caseData.getFamilyManCaseNumber()),
                    formatLocalDateToString(dateSubmitted,"dd-MM-yyyy"),
                    lastHearingDate,
                    String.valueOf(ChronoUnit.WEEKS.between(dateSubmitted, currentDate)),
                    hearingType,
                    formatLocalDateToString(dateSubmitted.plusWeeks(26),"dd-MM-yyyy"),
                    "</td></tr>")
            );
        }

        if (searchResult.getTotal() > 0) {
            result.append("</table>");
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


        Filter filter = Filter.builder().
                clauses(List.of(termQuery, termsQuery, rangeQuery))
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
