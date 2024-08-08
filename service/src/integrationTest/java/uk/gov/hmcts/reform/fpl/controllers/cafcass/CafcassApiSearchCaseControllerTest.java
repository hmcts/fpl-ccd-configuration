package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCase;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSearchCasesResponse;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiSearchCaseService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class CafcassApiSearchCaseControllerTest extends CafcassApiControllerBaseTest {
    @MockBean
    private CafcassApiSearchCaseService cafcassApiSearchCaseService;

    @Test
    void searchCases() throws Exception {
        List<CafcassApiCase> responseCases = List.of(
            CafcassApiCase.builder()
                .caseId(1L).jurisdiction("jurisdiction").state("state").caseTypeId("caseTypeId")
                .createdDate(LocalDateTime.of(2023, 1, 1, 10, 0, 0))
                .lastModified(LocalDateTime.of(2024, 3, 28, 12,32,0))
                .caseData(CafcassApiCaseData.builder().build())
                .build(),
            CafcassApiCase.builder()
                .caseId(2L).jurisdiction("jurisdiction").state("state").caseTypeId("caseTypeId")
                .createdDate(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
                .lastModified(LocalDateTime.of(2024, 3, 28, 12,40,0))
                .caseData(CafcassApiCaseData.builder().build())
                .build());

        when(cafcassApiSearchCaseService.searchCaseByDateRange(
            LocalDateTime.of(2024, 3, 28, 12,30,1,541000000),
            LocalDateTime.of(2024, 3, 28, 12,45,1,540000000))
        ).thenReturn(responseCases);

        MvcResult response = sendRequest(buildGetRequest("/cases")
            .queryParam("startDate", "2024-03-28T12:30:01.541")
            .queryParam("endDate", "2024-03-28T12:45:01.540"), 200);

        assertEquals(
            CafcassApiSearchCasesResponse.builder().total(2).cases(responseCases).build(),
            readResponseContent(response, CafcassApiSearchCasesResponse.class));
    }

    @Test
    void searchCasesInvalidFormat400() throws Exception {
        sendRequest(buildGetRequest("/cases")
            .queryParam("startDate", "123").queryParam("endDate", "321"),
            400);
    }

    @Test
    void shouldReturn400IfEmptyParam() throws Exception {
        sendRequest(buildGetRequest("/cases"), 400);

        sendRequest(buildGetRequest("/cases")
            .queryParam("startDate", "2023-03-28T12:32:54.541"), 400);

        sendRequest(buildGetRequest("/cases")
            .queryParam("endDate", "2024-03-27T12:32:54.542"), 400);
    }

    @Test
    void searchCasesInvalidTimeRange() throws Exception {
        sendRequest(buildGetRequest("/cases")
            .queryParam("startDate", "2024-03-28T12:32:54.541")
            .queryParam("endDate", "2023-03-27T12:32:54.542"), 400);
    }

    @Test
    void shouldReturn400IfSearchRangeMoreThan15Minutes() throws Exception {
        sendRequest(buildGetRequest("/cases")
            .queryParam("startDate", "2024-03-28T12:32:54.541")
            .queryParam("endDate", "2024-03-28T12:47:54.542"), 400);
    }
}
