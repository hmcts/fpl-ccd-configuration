package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.CMSReportEventData;
import uk.gov.hmcts.reform.fpl.service.search.SearchService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.elasticsearch.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;

@ExtendWith(MockitoExtension.class)
class CMSReportServiceTest {

    @Mock
    private SearchService searchService;

    @Mock
    private CaseConverter converter;

    @InjectMocks
    private CMSReportService service;


    @Test
    void shouldReturnReport() {

        CMSReportEventData cmsReportEventData = CMSReportEventData.builder()
                .swanseaDFJCourts("344")
                .build();


        CaseData caseDataSelected = CaseData.builder()
                .cmsReportEventData(cmsReportEventData)
                .build();

        List<Element<HearingBooking>> hearingDetails =
                ElementUtils.wrapElements(List.of(createHearingBooking()));

        List<CaseDetails> caseDetails = List.of(createCaseDetails());

        SearchResult searchResult = SearchResult.builder()
                .total(caseDetails.size())
                .cases(caseDetails)
                .build();


        when(searchService.search(any(), eq(50), eq(0), isA(Sort.class))).thenReturn(searchResult);
        when(converter.convert(isA(CaseDetails.class))).thenReturn(getCaseData(hearingDetails));
        String report = service.getReport(caseDataSelected);
        System.out.println(report);
    }

    private CaseDetails createCaseDetails() {
        Random random = new Random();
        return CaseDetails.builder()
                .id(random.nextLong())
                .build();
    }

    private CaseData getCaseData(List<Element<HearingBooking>> hearingDetails) {
        return CaseData.builder()
                .familyManCaseNumber("PO22ZA12345")
                .dateSubmitted(LocalDate.now().minusWeeks(24))
                .hearingDetails(hearingDetails)
                .build();
    }

    private HearingBooking createHearingBooking() {
        return HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .type(FINAL)
                .build();
    }

}