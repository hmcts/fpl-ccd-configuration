package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderSubtype;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.InterimEndDate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.stream.Stream;

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
    FixedTimeConfiguration.class})
class CareOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {
    @Autowired
    private CareOrderGenerationService service;

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseData(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = SEALED;
        CaseData caseData = createPopulatedCaseData(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    @ParameterizedTest
    @MethodSource("docmosisDataGenerationSource")
    void shouldGetTemplateDataWhenGivenPopulatedCaseDataInDraft(GeneratedOrderType type,
        GeneratedOrderSubtype subtype) {
        OrderStatus orderStatus = DRAFT;
        CaseData caseData = createPopulatedCaseData(subtype, orderStatus);

        DocmosisGeneratedOrder templateData = service.getTemplateData(caseData);

        DocmosisGeneratedOrder expectedData = createExpectedDocmosisData(type, subtype, orderStatus);
        assertThat(templateData).isEqualToComparingFieldByField(expectedData);
    }

    private static Stream<Arguments> docmosisDataGenerationSource() {
        return Stream.of(
            Arguments.of(CARE_ORDER, INTERIM),
            Arguments.of(CARE_ORDER, FINAL)
        );
    }

    @Override
    CaseDataBuilder populateCustomCaseData(GeneratedOrderSubtype subtype) {
        CaseDataBuilder caseDataBuilder = CaseData.builder()
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
            caseDataBuilder.interimEndDate(InterimEndDate.builder().type(END_OF_PROCEEDINGS).build());
        }
        return caseDataBuilder;
    }

    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(GeneratedOrderSubtype subtype) {
        List<DocmosisChild> children = getChildren();
        DocmosisGeneratedOrderBuilder<?,?> orderBuilder = DocmosisGeneratedOrder.builder();
        if (subtype == INTERIM) {
            orderBuilder = createOrderBuilder("Interim care order",
                "Section 38 Children Act 1989", "It is ordered that the children are "
                    + "placed in the care of Example Local Authority until the end of the proceedings.", children);
        } else if (subtype == FINAL) {
            orderBuilder = createOrderBuilder("Care order", "Section 31 Children Act 1989",
                "It is ordered that the children are placed in the care of "
                    + "Example Local Authority.", children);
        }
        return orderBuilder
            .orderType(CARE_ORDER)
            .localAuthorityName(LOCAL_AUTHORITY_NAME);
    }
}