package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.AppointedGuardianFormatter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AppointedGuardianBlockPrePopulatorTest {

    private static final String GUARDIAN_LABEL = "guardian label";
    private final AppointedGuardianFormatter appointedGuardianFormatter = mock(AppointedGuardianFormatter.class);

    private final AppointedGuardianBlockPrePopulator underTest = new AppointedGuardianBlockPrePopulator(
        appointedGuardianFormatter
    );

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.APPOINTED_GUARDIAN);
    }

    @Test
    void prePopulate() {
        List<Element<Respondent>> respondents = wrapElements(mock(Respondent.class));
        Others others = Others.builder().additionalOthers(wrapElements(mock(Other.class))).build();
        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .others(others)
            .build();

        when(appointedGuardianFormatter.getGuardiansLabel(caseData)).thenReturn(GUARDIAN_LABEL);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of(
                "appointedGuardianSelector", Selector.builder().count("12").build(),
                "appointedGuardians_label", GUARDIAN_LABEL
            )
        );
    }
}
