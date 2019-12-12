package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderServiceTest {

    private final CaseManagementOrderService service = new CaseManagementOrderService();

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
}
