package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.BaseDocmosisParameters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class C32CareOrderDocumentParameterGeneratorTest {

    private final C32CareOrderDocumentParameterGenerator underTest = new C32CareOrderDocumentParameterGenerator();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(Order.C32_CARE_ORDER);
    }

    @Test
    void generate() {
        assertThat(underTest.generate(CaseDetails.builder().build())).isEqualTo(
            BaseDocmosisParameters.builder()
                .title("title 1")
                .whatever("whatever")
                .build()
        );
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.ORDER);

    }
}
