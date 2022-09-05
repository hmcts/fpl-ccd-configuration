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
    private final CaseConverter converter;
    private final static List<HearingType> REQUIRED_HEARING_TYPE = List.of(
            CASE_MANAGEMENT, ISSUE_RESOLUTION, FINAL
    );
    private final static List<String> REQUIRED_STATES = List.of (
            "submitted","gatekeeping","prepare_for_hearing","final_hearing"
    );

    Function<String, String> headerField = fieldName ->  String.join("",
            "<th class='search-result-column-label'>",
            fieldName,
            "</th>");

    Function<String, String> cellField = fieldName ->  String.join("",
            "<td class='search-result-column-cell'>",
            fieldName,
            "</td>");

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
            result.append("<html><table>")
                    .append("<tr>")
                    .append(headerField.apply("Sr no."))
                    .append(headerField.apply("Case Number"))
                    .append(headerField.apply("Receipt date"))
                    .append(headerField.apply("Last hearing"))
                    .append(headerField.apply("Age of </br>case</br>(weeks)"))
                    .append(headerField.apply("PLO stage"))
                    .append(headerField.apply("Expected FH date"))
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
                String.join("",
                    "<tr>",
                    cellField.apply(String.valueOf(counter[0]++)),
                    cellField.apply(String.valueOf(caseData.getFamilyManCaseNumber())),
                    cellField.apply(formatLocalDateToString(dateSubmitted,"dd-MM-yyyy")),
                    cellField.apply(lastHearingDate),
                    cellField.apply(String.valueOf(ChronoUnit.WEEKS.between(dateSubmitted, currentDate))),
                    cellField.apply(hearingType),
                    cellField.apply(formatLocalDateToString(dateSubmitted.plusWeeks(26),"dd-MM-yyyy")),
                    "</tr>")
            );
        }

        if (searchResult.getTotal() > 0) {
            result.append("</table></html>");
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
