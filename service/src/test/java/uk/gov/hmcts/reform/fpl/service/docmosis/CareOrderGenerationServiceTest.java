package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CareOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenService.class})
class CareOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {
    @Autowired
    private CareOrderGenerationService service;

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = getCase(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(subtype, orderStatus);

        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @ParameterizedTest
    @EnumSource(GeneratedOrderSubtype.class)
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft(GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = getCase(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = getExpectedDocument(subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    private CaseData getCase(GeneratedOrderSubtype subtype, OrderStatus status) {
        CaseData.CaseDataBuilder caseBuilder = defaultCaseData(subtype, status)
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(CARE_ORDER)
                .subtype(subtype)
                .document(DocumentReference.builder().build())
                .build())
            .orderFurtherDirections(FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Example Directions")
                .build())
            .orderAppliesToAllChildren(YES.getValue());

        if (subtype == INTERIM) {
            caseBuilder.interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build());
        }

        return caseBuilder.build();
    }

    private DocmosisGeneratedOrder getExpectedDocument(GeneratedOrderSubtype subtype, OrderStatus orderStatus) {
        DocmosisGeneratedOrderBuilder orderBuilder = defaultExpectedData(CARE_ORDER, subtype, orderStatus)
            .children(getChildren())
            .orderType(CARE_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME);

        if (subtype == INTERIM) {
            orderBuilder
                .orderTitle("Interim care order")
                .childrenAct("Section 38 Children Act 1989")
                .orderDetails("It is ordered that the children are "
                    + "placed in the care of Example Local Authority until the end of the proceedings.");

        } else if (subtype == FINAL) {
            orderBuilder
                .orderTitle("Care order")
                .childrenAct("Section 31 Children Act 1989")
                .orderDetails("It is ordered that the children are placed in the care of Example Local Authority.");
        }

        return orderBuilder.build();
    }
}
