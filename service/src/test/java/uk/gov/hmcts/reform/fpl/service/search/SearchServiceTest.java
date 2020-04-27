package uk.gov.hmcts.reform.fpl.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class SearchServiceTest {

    @Autowired
    Time time;

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void shouldSearchCasesByDateProperty() {
        String property = "a.b";
        LocalDate date = time.now().toLocalDate();

        List<CaseDetails> expectedCases = List.of(CaseDetails.builder().id(nextLong()).build());

        when(coreCaseDataService.searchCases(any(), any())).thenReturn(expectedCases);

        List<CaseDetails> casesFound = searchService.search(property, date);

        assertThat(casesFound).isEqualTo(expectedCases);

        verify(coreCaseDataService).searchCases(eq("CARE_SUPERVISION_EPO"), queryCaptor.capture());

        String expectedQuery = format("{\"query\":{\"range\":{\"%s\":{\"lt\":\"%sT00:00\",\"gte\":\"%sT00:00\"}}}}",
            property, date.plusDays(1), date);

        JSONAssert.assertEquals(queryCaptor.getValue(), expectedQuery, NON_EXTENSIBLE);
    }

}
