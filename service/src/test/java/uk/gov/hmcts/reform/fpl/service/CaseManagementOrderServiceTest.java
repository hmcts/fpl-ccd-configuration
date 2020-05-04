package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;

//TODO: this will be slightly improved when 1480 is merged to master
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FixedTimeConfiguration.class, CaseManagementOrderService.class, HearingBookingService.class,
    DocumentService.class, DraftCMOService.class, CommonDirectionService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, StandardDirectionOrderGenerationService.class, CaseManagementOrderGenerationService.class,
    JsonOrdersLookupService.class, JacksonAutoConfiguration.class, HearingVenueLookUpService.class,
    DocmosisDocumentGeneratorService.class, RestTemplate.class, DocmosisConfiguration.class, UploadDocumentService.class
})
class CaseManagementOrderServiceTest {

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private DocumentUploadClientApi documentUploadClient;
    @MockBean
    private RequestData requestData;

    @Autowired
    private Time time;

    @Autowired
    private CaseManagementOrderService service;

    @Test
    void shouldExtractExpectedMapFieldsWhenAllDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .recitals(emptyList())
            .action(OrderAction.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenPartialDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(null);

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldGetCMOIssueDate() {
        LocalDate expectedIssueDate = LocalDate.now().minusDays(1);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .dateOfIssue(expectedIssueDate.format(DateTimeFormatter.ofPattern(DATE)))
            .build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(expectedIssueDate);
    }

    @Test
    void shouldGetDefaultCMOIssueDate() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }

    @Test
    void shouldGetDefaultCMOIssueDateWhenCMODoesNotExists() {
        LocalDate issueDate = service.getIssuedDate(null);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }
}
