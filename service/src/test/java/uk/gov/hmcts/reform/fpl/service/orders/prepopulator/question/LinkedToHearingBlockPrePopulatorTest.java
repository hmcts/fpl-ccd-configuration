package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class LinkedToHearingBlockPrePopulatorTest {

    private static final List<Element<HearingBooking>> HEARINGS = List.of(
        element(UUID.randomUUID(), mock(HearingBooking.class)));
    private static final DynamicList DYNAMIC_LIST = mock(DynamicList.class);
    private final HearingService hearingService = mock(HearingService.class);

    private final LinkedToHearingBlockPrePopulator underTest = new LinkedToHearingBlockPrePopulator(hearingService);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.LINKED_TO_HEARING);
    }

    @Test
    void prePopulate() {
        CaseData caseData = mock(CaseData.class);
        when(hearingService.findOnlyHearingsInPast(caseData)).thenReturn(HEARINGS);
        when(caseData.buildDynamicHearingList(HEARINGS,null)).thenReturn(DYNAMIC_LIST);

        Map<String, Object> actual = underTest.prePopulate(caseData);

        assertThat(actual).isEqualTo(Map.of("manageOrdersApprovedAtHearingList", DYNAMIC_LIST));
    }
}
