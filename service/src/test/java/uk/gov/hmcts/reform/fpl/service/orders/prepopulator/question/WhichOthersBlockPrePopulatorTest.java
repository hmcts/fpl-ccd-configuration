package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.OthersService;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class WhichOthersBlockPrePopulatorTest {

    private static final String OTHER_LABEL = "other label";
    private final OthersService othersService = mock(OthersService.class);

    private final WhichOthersBlockPrePopulator underTest = new WhichOthersBlockPrePopulator(othersService);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.WHICH_OTHERS);
    }

    @Test
    void prePopulate() {
        Others others = Others.builder()
            .firstOther(Other.builder().name("Ed").activeParty(YES.getValue()).build())
            .additionalOthers(wrapElements(
                Other.builder().name("Edd").activeParty(YES.getValue()).build(),
                Other.builder().name("Eddy").activeParty(NO.getValue()).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .others(others)
            .build();

        when(othersService.getOthersLabel(any())).thenReturn(OTHER_LABEL);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                "othersSelector", Selector.builder().count("12").build(),
                "others_label", OTHER_LABEL
            )
        );
    }

}
