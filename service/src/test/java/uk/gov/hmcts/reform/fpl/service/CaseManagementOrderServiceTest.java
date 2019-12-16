package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingDynmaicList;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, JsonOrdersLookupService.class, LookupTestConfig.class,
    JsonOrdersLookupService.class, DateFormatterService.class, DirectionHelperService.class,
    DocmosisConfiguration.class, RestTemplate.class, CaseDataExtractionService.class,
    DocmosisDocumentGeneratorService.class, CommonCaseDataExtractionService.class, HearingBookingService.class,
    HearingVenueLookUpService.class, CaseManagementOrderService.class
})
class CaseManagementOrderServiceTest {
    private static final UUID HEARING_BOOKING_ID = fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31");

    @Autowired
    private CaseManagementOrderService service;

    @Test
    void shouldAddDocumentToOrderWhenDocumentExists() throws IOException {
        CaseManagementOrder orderWithDoc = service.addDocument(CaseManagementOrder.builder().build(), document());

        assertThat(orderWithDoc.getOrderDoc()).isEqualTo(buildFromDocument(document()));
    }

    @Test
    void shouldAddActionToOrderWhenActionExists() {
        CaseManagementOrder orderWithDoc = service.addAction(CaseManagementOrder.builder().build(),
            OrderAction.builder().type(SELF_REVIEW).build());

        assertThat(orderWithDoc.getAction()).isEqualTo(OrderAction.builder().type(SELF_REVIEW).build());
    }

    @Test
    void shouldExtractExpectedMapFieldsWhenAllDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .recitals(emptyList())
            .action(OrderAction.builder().build())
            .build());

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void shouldExtractMapFieldsWhenPartialDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .build());

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void shouldExtractMapFieldsWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(null);

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void shouldSetOrderActionNextHearingDateWhenProvidedNextHearingDateList() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        CaseManagementOrder updatedCaseManagementOrder =
            service.addNextHearingToCMO(createHearingBookingDynmaicList(), caseManagementOrder);

        NextHearing nextHearing = updatedCaseManagementOrder.getNextHearing();

        assertThat(nextHearing.getId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(nextHearing.getDate()).isEqualTo("15th Dec 2019");
    }

    @Test
    void shouldPreserveCMOWhenNextHearingDateListIsNotProvided() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate("Test date")
            .build();

        CaseManagementOrder updatedCaseManagementOrder = service.addNextHearingToCMO(null, caseManagementOrder);

        assertThat(updatedCaseManagementOrder.getHearingDate()).isEqualTo("Test date");
    }

    private CaseManagementOrder createCMOWithNextHearing() {
        return CaseManagementOrder.builder()
            .nextHearing(NextHearing.builder()
                .id(HEARING_BOOKING_ID)
                .build())
            .build();
    }
}
