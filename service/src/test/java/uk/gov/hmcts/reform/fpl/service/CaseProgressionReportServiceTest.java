package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @InjectMocks
    private CaseProgressionReportService service;


    @Test
    void shouldReturnHtmlReport() {
        LocalDate submittedDate = LocalDate.now().minusWeeks(24);
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType("MISSING_TIMETABLE")
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<Element<HearingBooking>> hearingDetails =
                ElementUtils.wrapElements(List.of(createHearingBooking(FINAL, LocalDateTime.of(submittedDate.plusDays(24), LocalTime.now()))));

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(caseDetails.size())
                .cases(caseDetails)
                .build();

        when(searchService.search(any(), eq(100), eq(0), isA(Sort.class))).thenReturn(searchResult);
        when(converter.convert(isA(CaseDetails.class))).thenReturn(getCaseData(hearingDetails, submittedDate));
        String report = service.getHtmlReport(caseDataSelected);
        System.out.println(report);
    }

    @Test
    void shouldReturnFileReport() throws IOException {
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-11-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .reportType("MISSING_TIMETABLE")
                .build();

        CaseData caseDataSelected = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        List<Element<HearingBooking>> hearingDetails = ElementUtils.wrapElements(List.of(createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)));

        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingDetailsTwo = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, caseManagementHearing),
                        createHearingBooking(ISSUE_RESOLUTION, issueHearing)));

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(100)
                .cases(caseDetails)
                .build();

        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(0), isA(Sort.class))).thenReturn(searchResult);
        when(searchService.search(any(), eq(ES_DEFAULT_SIZE), eq(ES_DEFAULT_SIZE), isA(Sort.class))).thenReturn(searchResult);
        when(converter.convert(isA(CaseDetails.class))).thenReturn(
                getCaseData(hearingDetails, submittedDate, "PO22ZA12345"),
                getCaseData(hearingDetailsTwo, submittedDate, "ZO88ZA56789")
                );
        Optional<File> fileReport = service.getFileReport(caseDataSelected);
        assertThat(fileReport).isPresent();

        List<CSVRecord> csvRecordsList = readCsv(fileReport.get());
        assertThat(csvRecordsList)
                .isNotEmpty()
                .hasSize(3)
                .extracting(record -> tuple(
                        record.get(0), record.get(1), record.get(2), record.get(3), record.get(4),
                        record.get(5), record.get(6))
                ).containsExactly(
                        tuple("Case Number", "Receipt date", "Last hearing", "Next hearing",
                                "Age of case (weeks)", "PLO stage", "Expected FH date"),
                        tuple("PO22ZA12345", "04-05-2022", "22-09-2022",
                                "03-11-2022", "19", "Final", "02-11-2022"),
                        tuple("ZO88ZA56789", "04-05-2022", "18-06-2022",
                                "22-09-2022", "19", "Issue resolution", "02-11-2022")
                );
        verify(searchService, times(2))
                .search(isA(ESQuery.class), anyInt(), anyInt(), isA(Sort.class));
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
        return getCaseData(hearingDetails, submittedDate, "PO22ZA12345");
    }

    private CaseData getCaseData(List<Element<HearingBooking>> hearingDetails, LocalDate submittedDate, String familyManNum) {
        return CaseData.builder()
                .familyManCaseNumber(familyManNum)
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
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("03-11-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(List.of(createHearingBooking(FINAL, finalHearing),
                createHearingBooking(ISSUE_RESOLUTION, issueHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isPresent());
        assertThat(hearingInfo.get().getPloStage()).isEqualTo(FINAL.getLabel());
        assertThat(hearingInfo.get().getLastHearing()).isEqualTo("22-09-2022");
        assertThat(hearingInfo.get().getNextHearing()).isEqualTo("03-11-2022");
    }

    @Test
    void shouldReturnHearingWithEmptyFinalHearingDetail() {
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime finalHearing = LocalDateTime.of(
                LocalDate.parse("01-11-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-10-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("22-09-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime issueHearing = LocalDateTime.of(
                LocalDate.parse("20-09-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime caseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-09-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime firstCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("22-08-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime secondCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("18-06-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        LocalDate submittedDate = LocalDate.parse("04-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDateTime firstCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("26-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());
        LocalDateTime secondCaseManagementHearing = LocalDateTime.of(
                LocalDate.parse("27-05-2022" , DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalTime.now());

        List<Element<HearingBooking>> hearingBooking = ElementUtils.wrapElements(
                List.of(createHearingBooking(CASE_MANAGEMENT, firstCaseManagementHearing),
                        createHearingBooking(CASE_MANAGEMENT, secondCaseManagementHearing)));
        CaseData caseData = getCaseData(hearingBooking, submittedDate);

        Optional<HearingInfo> hearingInfo = service.getHearingInfo(caseData);
        assertTrue(hearingInfo.isEmpty());
    }
}