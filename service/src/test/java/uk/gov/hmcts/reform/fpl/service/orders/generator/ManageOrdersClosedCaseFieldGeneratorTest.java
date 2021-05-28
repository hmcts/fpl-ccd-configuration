package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

@ExtendWith({MockitoExtension.class})
public class ManageOrdersClosedCaseFieldGeneratorTest {
    private static final LocalDate TODAY = LocalDate.of(2012, 12, 22);
    private static final LocalDateTime NOW = TODAY.atStartOfDay();

    @Mock
    private Time time;

    @Mock
    private ChildrenService childrenService;

    @InjectMocks
    private ManageOrdersClosedCaseFieldGenerator underTest;

    @Test
    void shouldCloseCase() {
        CaseData caseData = buildCaseData("Yes", C32_CARE_ORDER);

        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());
        when(time.now()).thenReturn(NOW);

        Map<String, Object> generatedData = underTest.generate(caseData);
        Map<String, Object> expectedData = Map.of(
            "state", "CLOSED",
            "closeCaseTabField", CloseCase.builder().date(time.now().toLocalDate()).build()
        );

        assertThat(generatedData).containsAllEntriesOf(expectedData);
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldUpdateChildrenAndNotCloseCase() {

        CaseData caseData = buildCaseData("No", C32_CARE_ORDER);
        when(childrenService.updateFinalOrderIssued(caseData))
            .thenReturn(Collections.emptyList());

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).doesNotContainEntry("state", "CLOSED");
        assertThat(generatedData).doesNotContainKey("closeCaseTabField");
        assertThat(generatedData).containsKey("children1");
    }

    @Test
    void shouldNotUpdateChildrenWhenNotFinalOrder() {
        CaseData caseData = buildCaseData("No", C21_BLANK_ORDER);

        Map<String, Object> generatedData = underTest.generate(caseData);

        assertThat(generatedData).isEqualTo(Collections.emptyMap());
    }

    private CaseData buildCaseData(String closeCase, Order order) {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersCloseCase(closeCase)
            .manageOrdersType(order)
            .build();

        return CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .build();
    }
}
