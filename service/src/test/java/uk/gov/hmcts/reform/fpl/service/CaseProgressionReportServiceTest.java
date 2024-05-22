package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.exceptions.CaseProgressionReportException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingInfo;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.ESQuery;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Sort;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseProgressionReportType.AT_RISK;
import static uk.gov.hmcts.reform.fpl.enums.CaseProgressionReportType.MISSING_TIMETABLE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.service.search.SearchService.ES_DEFAULT_SIZE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(MockitoExtension.class)
class CaseProgressionReportServiceTest {

    @Mock
    private SearchService searchService;

    @Mock
    private CaseConverter converter;

    @Mock
    private CourtService courtService;

    @InjectMocks
    private CaseProgressionReportService service;

    @ParameterizedTest
    @MethodSource("provideCourtDetails")
    void shouldReturnHtmlReport(String courtId, Optional<Court> court, String courtName) {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-11-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts(courtId)
                .reportType(AT_RISK)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(
            List.of(
                createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)
            ));


        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        when(courtService.getCourt(courtId)).thenReturn(court);

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        when(converter.convert(isA(CaseDetails.class))).thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342447124290L)
        );

        String report = service.getHtmlReport(caseDataSelected);
        assertThat(report).isEqualTo("<table>"
                + "<tr><th class='search-result-column-label' colspan=\"9\">"
                +  courtName
                + "<br>"
                + "Please note: only the top 100 cases can be displayed on this screen. "
                + "To see all cases, please select continue on this page, "
                + "select done on the next page and a full list of cases "
                + "will be automatically emailed to you."
                + "<th class='search-result-column-label'></tr></table>"
                + "<table><tr><th class='search-result-column-label'>Sr no.</th>"
                + "<th class='search-result-column-label'>Case Number</th>"
                + "<th class='search-result-column-label'>CCD Number</th>"
                + "<th class='search-result-column-label'>Receipt date</th>"
                + "<th class='search-result-column-label'>Last PLO hearing</th>"
                + "<th class='search-result-column-label'>Next hearing</th>"
                + "<th class='search-result-column-label'>Age of </br>case</br>(weeks)</th>"
                + "<th class='search-result-column-label'>PLO stage</th>"
                + "<th class='search-result-column-label'>Expected FH date</th></tr>"
                + "<tr><td class='search-result-column-cell'>1</td>"
                + "<td class='search-result-column-cell'>PO22ZA12345</td>"
                + "<td class='search-result-column-cell'>1663342447124290</td>"
                + "<td class='search-result-column-cell'>04-05-2022</td>"
                + "<td class='search-result-column-cell'>22-09-2022</td>"
                + "<td class='search-result-column-cell'>03-11-2022</td>"
                + "<td class='search-result-column-cell'>" + getWeeks(submittedDate) + "</td>"
                + "<td class='search-result-column-cell'>Final</td>"
                + "<td class='search-result-column-cell'>02-11-2022</td></tr>"
                + "</table>");
    }

    private String getWeeks(LocalDate submittedDate) {
        return String.valueOf(ChronoUnit.WEEKS.between(submittedDate, LocalDate.now()));
    }

    @Test
    void shouldReturnHtmlReportWithEmptyTableWhenNoResultFound() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(MISSING_TIMETABLE)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(0)
                .cases(caseDetails)
                .build();

        when(courtService.getCourt("344"))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);


        String report = service.getHtmlReport(caseDataSelected);
        assertThat(report).isEqualTo("<table>"
                + "<tr><th class='search-result-column-label' colspan=\"9\">"
                + "Family court Swansea<th class='search-result-column-label'>"
                + "</tr>"
                + "</table>");
    }

    @Test
    void shouldThrowExceptionWhenEmptyResultSet() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(AT_RISK)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        when(courtService.getCourt("344"))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        assertThatThrownBy(() -> service.getHtmlReport(caseDataSelected))
                .isInstanceOf(CaseProgressionReportException.class);
    }

    @Test
    void shouldThrowExceptionWhenReportTypeunknown() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        assertThatThrownBy(() -> service.getHtmlReport(caseDataSelected))
                .isInstanceOf(CaseProgressionReportException.class);
    }

    @Test
    void shouldReturnHtmlReportWithEmptyTableWhenCasesHearingAreWithinThreshold() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(AT_RISK)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-06-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(
                List.of(
                        createHearingBooking(FINAL, finalHearing)
                ));

        when(courtService.getCourt("344"))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        when(converter.convert(isA(CaseDetails.class))).thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342447124290L)
        );

        String report = service.getHtmlReport(caseDataSelected);
        assertThat(report).isEqualTo("<table>"
                + "<tr><th class='search-result-column-label' colspan=\"9\">"
                + "Family court Swansea<th class='search-result-column-label'>"
                + "</tr>"
                + "</table>");
    }

    private static Stream<Arguments> provideCourtDetails() {
        return Stream.of(
                Arguments.of("344",
                        Optional.of(Court.builder().name("Family court Swansea").build()),
                        "Family court Swansea"),
                Arguments.of("123",
                        Optional.empty(),
                        "Court name not found")
        );
    }

    @Test
    void shouldReturnFileReport() throws IOException {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-11-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(MISSING_TIMETABLE)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(
            List.of(
                createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)
            )
        );

        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        LocalDateTime firstCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("22-08-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingDetailsTwo = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, caseManagementHearing),
                        createHearingBooking(ISSUE_RESOLUTION, issueHearing)));

        List<Element<HearingBooking>> hearingDetailsThree = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, firstCaseManagementHearing)));
        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(150)
                .cases(caseDetails)
                .build();

        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(0), isA(Sort.class)))
            .thenReturn(searchResult);
        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(ES_DEFAULT_SIZE), isA(Sort.class)))
            .thenReturn(searchResult);
        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(ES_DEFAULT_SIZE * 2), isA(Sort.class)))
            .thenReturn(searchResult);
        when(converter.convert(isA(CaseDetails.class)))
            .thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342447124290L),
                getCaseData(hearingDetailsTwo, submittedDate.minusMonths(1), "ZO88ZA56789", 1663342966373807L),
                getCaseData(hearingDetailsThree, submittedDate.plusMonths(2), "AO88ZA56789", 1663342966000000L)
            );

        Optional<File> fileReport = service.getFileReport(caseDataSelected);
        assertThat(fileReport).isPresent();

        List<CSVRecord> csvRecordsList = readCsv(fileReport.get());
        assertThat(csvRecordsList)
            .isNotEmpty()
            .hasSize(4)
            .extracting(record -> tuple(
                record.get(0), record.get(1), record.get(2), record.get(3), record.get(4),
                record.get(5), record.get(6), record.get(7))
            ).containsExactly(
                tuple("Case Number", "CCD Number","Receipt date", "Last PLO hearing", "Next hearing",
                        "Age of case (weeks)","PLO stage", "Expected FH date"),
                tuple("PO22ZA12345", "1663342447124290", "04-05-2022", "22-09-2022",
                        "03-11-2022", getWeeks(submittedDate), "Final", "02-11-2022"),
                tuple("ZO88ZA56789", "1663342966373807", "04-04-2022", "18-06-2022",
                        "22-09-2022", getWeeks(submittedDate.minusMonths(1)), "Issue resolution", "03-10-2022"),
                tuple("AO88ZA56789", "1663342966000000", "04-07-2022", "22-08-2022",
                        "-", getWeeks(submittedDate.plusMonths(2)), "Case management", "02-01-2023")
            );
        verify(searchService, times(3))
                .search(isA(ESQuery.class), anyInt(), anyInt(), isA(Sort.class));
    }

    @Test
    void shouldReturnFileReportWithFutureCaseManagement() throws IOException {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(MISSING_TIMETABLE)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();


        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2050", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, caseManagementHearing)));

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(0), isA(Sort.class)))
            .thenReturn(searchResult);

        when(converter.convert(isA(CaseDetails.class)))
            .thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342447124290L)
            );

        Optional<File> fileReport = service.getFileReport(caseDataSelected);
        assertThat(fileReport).isPresent();

        List<CSVRecord> csvRecordsList = readCsv(fileReport.get());
        assertThat(csvRecordsList)
            .isNotEmpty()
            .hasSize(2)
            .extracting(record -> tuple(
                record.get(0), record.get(1), record.get(2), record.get(3), record.get(4),
                record.get(5), record.get(6), record.get(7))
            ).containsExactly(
                tuple("Case Number", "CCD Number","Receipt date", "Last PLO hearing", "Next hearing",
                        "Age of case (weeks)","PLO stage", "Expected FH date"),
                tuple("PO22ZA12345", "1663342447124290", "04-05-2022", "-",
                        "18-06-2050", getWeeks(submittedDate), "Case management", "02-11-2022")
            );
        verify(searchService)
                .search(isA(ESQuery.class), anyInt(), anyInt(), isA(Sort.class));
    }

    @Test
    void shouldThrowExceptionWhenEmptyResultSetForFileGeneration() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(AT_RISK)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        when(courtService.getCourt("344"))
                .thenReturn(Optional.of(Court.builder().name("Family court Swansea").build()));

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        assertThatThrownBy(() -> service.getFileReport(caseDataSelected))
                .isInstanceOf(CaseProgressionReportException.class);
    }


    @Test
    void shouldThrowExceptionWhenReportTypeUnknownForFileGeneration() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        assertThatThrownBy(() -> service.getFileReport(caseDataSelected))
                .isInstanceOf(CaseProgressionReportException.class);
    }

    @Test
    void shouldReturnEmptyFileWhenNoRecordFound() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(MISSING_TIMETABLE)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(0)
                .cases(caseDetails)
                .build();
        when(searchService.search(any(), eq(50), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        Optional<File> fileReport = service.getFileReport(caseDataSelected);
        assertThat(fileReport).isEmpty();
    }


    @Test
    void shouldReturnEmptyFileOptionalWhenCasesHearingAreWithinThresholdForFile() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType(AT_RISK)
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(1)
                .cases(caseDetails)
                .build();

        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-06-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(
                List.of(
                        createHearingBooking(FINAL, finalHearing)
                ));

        when(searchService.search(any(), eq(50), eq(0), isA(Sort.class)))
                .thenReturn(searchResult);

        when(converter.convert(isA(CaseDetails.class))).thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342447124290L)
        );

        Optional<File> fileReport = service.getFileReport(caseDataSelected);
        assertThat(fileReport).isEmpty();
    }

    private List<CSVRecord> readCsv(File file) throws IOException {
        return CSVFormat.DEFAULT.parse(new FileReader(file)).getRecords();
    }

    private CaseDetails createCaseDetails() {
        Random random = new Random();
        return CaseDetails.builder()
                .id(random.nextLong())
                .build();
    }

    private CaseData getCaseData(List<Element<HearingBooking>> hearingDetails, LocalDate submittedDate) {
        return getCaseData(hearingDetails, submittedDate, "PO22ZA12345", 1663342966373807L);
    }

    private CaseData getCaseData(List<Element<HearingBooking>> hearingDetails,
                                 LocalDate submittedDate, String familyManNum, Long caseId) {
        return CaseData.builder()
                .familyManCaseNumber(familyManNum)
                .id(caseId)
                .dateSubmitted(submittedDate)
                .hearingDetails(hearingDetails)
                .build();
    }

    private HearingBooking createHearingBooking(HearingType type, LocalDateTime startTime) {
        return HearingBooking.builder()
                .startDate(startTime)
                .type(type)
                .build();
    }

    @Test
    void shouldReturnHearingWithFinalHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-11-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
            List.of(
                createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)
            )
        );

        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isPresent());
        assertThat(hearingInfo.get().getPloStage()).isEqualTo(FINAL.getLabel());
        assertThat(hearingInfo.get().getLastHearing()).isEqualTo("22-09-2022");
        assertThat(hearingInfo.get().getNextHearing()).isEqualTo("03-11-2022");
    }

    @Test
    void shouldReturnHearingWithEmptyFinalHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("01-11-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-10-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(List.of(
                createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)));

        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isEmpty());
    }

    @Test
    void shouldReturnHearingWithIssueResolutionHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, caseManagementHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isPresent());
        assertThat(hearingInfo.get().getPloStage()).isEqualTo(ISSUE_RESOLUTION.getLabel());
        assertThat(hearingInfo.get().getLastHearing()).isEqualTo("18-06-2022");
        assertThat(hearingInfo.get().getNextHearing()).isEqualTo("22-09-2022");
    }

    @Test
    void shouldReturnHearingWithEmptyIssueResolutionHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("20-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-09-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, caseManagementHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isEmpty());
    }


    @Test
    void shouldReturnHearingWithCaseManagementHearingDetailLastHearingDate() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime firstCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("22-08-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime secondCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, firstCaseManagementHearing),
                        createHearingBooking(CASE_MANAGEMENT, secondCaseManagementHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isPresent());
        assertThat(hearingInfo.get().getPloStage()).isEqualTo(CASE_MANAGEMENT.getLabel());
        assertThat(hearingInfo.get().getLastHearing()).isEqualTo("22-08-2022");
    }

    @Test
    void shouldReturnHearingWithCaseManagementHearingDetailNextHearingDate() {
        LocalDate submittedDate = LocalDate.now().minusWeeks(24);

        LocalDateTime firstCaseManagementHearing = LocalDateTime.now().plusDays(30);

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, firstCaseManagementHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isPresent());
        assertThat(hearingInfo.get().getPloStage()).isEqualTo(CASE_MANAGEMENT.getLabel());
        assertThat(hearingInfo.get().getNextHearing())
                .isEqualTo(formatLocalDateToString(firstCaseManagementHearing.toLocalDate(), "dd-MM-yyyy"));
    }

    @Test
    void shouldReturnHearingWithEmptyCaseManagementHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime firstCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("26-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime secondCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("27-05-2022", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, firstCaseManagementHearing),
                        createHearingBooking(CASE_MANAGEMENT, secondCaseManagementHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isEmpty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCourtIdNotSet() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .build();
        assertThatThrownBy(() -> service.getCourt(caseProgressionReportEventData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Court not found");
    }

    @Test
    void shouldReturnValidCourtIdWhenCourtDFJSet() {
        String courtId = "123";
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .liverpoolDFJCourts(courtId)
                .build();

        String actualCourtId = service.getCourt(caseProgressionReportEventData);
        assertThat(actualCourtId).isEqualTo(courtId);
    }
}
