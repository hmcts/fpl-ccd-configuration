package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BlankOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class})
class BlankOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private BlankOrderGenerationService service;

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseData() {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(BLANK_ORDER, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft() {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(null, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(BLANK_ORDER, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @Override
    CaseData.CaseDataBuilder populateCustomCaseData(GeneratedOrderSubtype subtype) {
        return CaseData.builder()
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build())
            .order(GeneratedOrder.builder()
                .title("Example Title")
                .details("Example details")
                .build())
            .orderAppliesToAllChildren(YES.getValue());
    }

    @SuppressWarnings("rawtypes")
    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(GeneratedOrderSubtype subtype) {
        return createOrderBuilder("Example Title", "Children Act 1989",
            "Example details", getChildren()).orderType(BLANK_ORDER);
    }
}
