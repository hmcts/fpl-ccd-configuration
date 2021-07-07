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
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.model.order.generated.OrderExclusionClause;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;
import uk.gov.hmcts.reform.fpl.utils.ChildSelectionUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype.INTERIM;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CareOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenSmartSelector.class, ChildrenService.class, CaseDetailsHelper.class,
    ChildSelectionUtils.class})
class CareOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    private static final String EXAMPLE_EXCLUSION_CLAUSE = "Example Exclusion Clause";
    private static final OrderStatus ORDER_STATUS = DRAFT;

    @Autowired
    private CareOrderGenerationService service;

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInterim() {
        CaseData caseData = getCase(INTERIM, ORDER_STATUS);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = enrichWithStandardData(CARE_ORDER, INTERIM, ORDER_STATUS,
            DocmosisGeneratedOrder.builder()
                .children(getChildren())
                .orderType(CARE_ORDER)
                .localAuthorityName(LOCAL_AUTHORITY_NAME)
                .orderTitle("Interim care order")
                .childrenAct("Section 38 Children Act 1989")
                .exclusionClause(EXAMPLE_EXCLUSION_CLAUSE)
                .orderDetails(String.format("It is ordered that the children are placed in the care of %s "
                    + "until the end of the proceedings, or until a further order is made.", LOCAL_AUTHORITY_NAME))
                .build());

        assertThat(templateData).isEqualTo(expectedData);
    }

    @Test
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataFinal() {
        CaseData caseData = getCase(FINAL, ORDER_STATUS);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        var orderBuilder = DocmosisGeneratedOrder.builder()
            .children(getChildren())
            .orderType(CARE_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME)
            .orderTitle("Care order")
            .childrenAct("Section 31 Children Act 1989")
            .orderDetails(format("It is ordered that the children are placed in the care of %s.",
                LOCAL_AUTHORITY_NAME));

        DocmosisGeneratedOrder expectedData = enrichWithStandardData(
            CARE_ORDER, INTERIM, ORDER_STATUS, orderBuilder.build()
        );

        assertThat(templateData).isEqualTo(expectedData);
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
            .orderAppliesToAllChildren(YES.getValue())
            .orderExclusionClause(OrderExclusionClause.builder()
                .exclusionClauseNeeded("Yes")
                .exclusionClause(EXAMPLE_EXCLUSION_CLAUSE)
                .build())
            .interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build());

        return caseBuilder.build();
    }

}
