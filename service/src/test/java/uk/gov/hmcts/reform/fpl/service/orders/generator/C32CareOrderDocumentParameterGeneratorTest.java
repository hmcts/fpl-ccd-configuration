package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C32CareOrderDocmosisParameters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class C32CareOrderDocumentParameterGeneratorTest {

    private final C32CareOrderDocumentParameterGenerator underTest = new C32CareOrderDocumentParameterGenerator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C32_CARE_ORDER);
    }

    @Test
    void generateDraft() {
        assertThat(underTest.generate(CaseData.builder().build(), OrderStatus.DRAFT)).isEqualTo(
            C32CareOrderDocmosisParameters.builder().build()
        );
    }

    @Test
    void generateSealed() {
        assertThat(underTest.generate(CaseData.builder().build(), OrderStatus.SEALED)).isEqualTo(
            C32CareOrderDocmosisParameters.builder().build()
        );
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER);
    }
}
