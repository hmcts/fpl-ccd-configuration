package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.interfaces.RemovableOrder;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class SDOOrderRemovalActionTest {

    private static final UUID TO_REMOVE_ORDER_ID = UUID.randomUUID();
    private static final UUID ALREADY_REMOVED_ORDER_ID = UUID.randomUUID();
    private static final String REASON = "Reason";
    private final SDOOrderRemovalAction underTest = new SDOOrderRemovalAction();

    @Test
    void isAcceptedIfStandardDirectionOrder() {
        assertThat(underTest.isAccepted(mock(StandardDirectionOrder.class))).isTrue();
    }

    @Test
    void isNotAcceptedIfAnyOtherClass() {
        assertThat(underTest.isAccepted(mock(RemovableOrder.class))).isFalse();
    }

    @Test
    void shouldThrowExceptionIfOrderNotFound() {
        StandardDirectionOrder emptyStandardDirectionOrder = StandardDirectionOrder.builder().build();

        CaseData caseData = CaseData.builder()
            .reasonToRemoveOrder(REASON)
            .noticeOfProceedings(null)
            .build();
        Map<String, Object> data = Maps.newHashMap();

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> underTest.action(caseData, data, ALREADY_REMOVED_ORDER_ID, emptyStandardDirectionOrder));

        assertThat(exception.getMessage()).isEqualTo(
            format("Failed to find order matching id %s", ALREADY_REMOVED_ORDER_ID)
        );
    }
}
