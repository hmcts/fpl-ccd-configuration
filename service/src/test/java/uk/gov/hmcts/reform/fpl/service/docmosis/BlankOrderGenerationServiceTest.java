package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BlankOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenSmartSelector.class, ChildrenService.class, CaseDetailsHelper.class})
class BlankOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private BlankOrderGenerationService service;

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(OrderStatus orderStatus) {
        CaseData caseData = getCase(orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    private CaseData getCase(OrderStatus orderStatus) {
        return defaultCaseData(orderStatus)
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(BLANK_ORDER)
                .document(DocumentReference.builder().build())
                .build())
            .order(GeneratedOrder.builder()
                .title("Example Title")
                .details("Example details")
                .build())
            .orderAppliesToAllChildren(YES.getValue())
            .build();
    }

    private DocmosisGeneratedOrder getExpectedDocument(OrderStatus orderStatus) {
        DocmosisGeneratedOrder orderBuilder = DocmosisGeneratedOrder.builder()
            .orderTitle("Example Title")
            .childrenAct("Children Act 1989")
            .orderDetails("Example details")
            .children(getChildren())
            .build();

        return enrichWithStandardData(BLANK_ORDER, orderStatus, orderBuilder);
    }
}
