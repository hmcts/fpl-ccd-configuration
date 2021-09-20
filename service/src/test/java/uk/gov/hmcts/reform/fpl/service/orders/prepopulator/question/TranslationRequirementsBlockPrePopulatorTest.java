package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationRequirementsBlockPrePopulatorTest {

    private final TranslationRequirementsBlockPrePopulator underTest = new TranslationRequirementsBlockPrePopulator();

    @Test
    void prePopulateWhenLanguageEnabled() {
        Map<String, Object> actual = underTest.prePopulate(CaseData.builder()
                .manageOrdersEventData(ManageOrdersEventData.builder()
                    .orderTempQuestions(OrderTempQuestions.builder()
                        .translationRequirements("ANY")
                        .build())
                    .build())
                .languageRequirement(YesNo.YES.getValue())
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "orderTempQuestions", OrderTempQuestions.builder()
                .translationRequirements("YES")
                .build()
        ));
    }

    @Test
    void prePopulateWhenLanguageDisabled() {
        Map<String, Object> actual = underTest.prePopulate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .orderTempQuestions(OrderTempQuestions.builder()
                    .translationRequirements("ANY")
                    .build())
                .build())
            .languageRequirement(YesNo.NO.getValue())
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "orderTempQuestions", OrderTempQuestions.builder()
                .translationRequirements("NO")
                .build()
        ));
    }

    @Test
    void prePopulateWhenLanguageNotSpecified() {
        Map<String, Object> actual = underTest.prePopulate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .orderTempQuestions(OrderTempQuestions.builder()
                    .translationRequirements("ANY")
                    .build())
                .build())
            .languageRequirement(null)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "orderTempQuestions", OrderTempQuestions.builder()
                .translationRequirements("NO")
                .build()
        ));
    }
}
