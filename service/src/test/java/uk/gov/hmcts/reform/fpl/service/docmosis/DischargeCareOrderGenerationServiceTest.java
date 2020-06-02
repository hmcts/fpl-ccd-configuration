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
import uk.gov.hmcts.reform.fpl.model.CaseData.CaseDataBuilder;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.selector.CareOrderSelector;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.GIRL;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@SuppressWarnings("unchecked")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DischargeCareOrderGenerationService.class, CaseDataExtractionService.class,
    LookupTestConfig.class, HearingVenueLookUpService.class, JacksonAutoConfiguration.class,
    FixedTimeConfiguration.class, ChildrenService.class, DischargeCareOrderService.class})
class DischargeCareOrderGenerationServiceTest extends AbstractOrderGenerationServiceTest {

    @Autowired
    private DischargeCareOrderGenerationService service;

    private GeneratedOrder orderWithMultipleChildren = GeneratedOrder.builder()
        .dateOfIssue("1 Jun 2020")
        .children(List.of(
            testChild("John", "Smith", BOY, LocalDate.of(2018, 1, 1)),
            testChild("Eva", "Smith", GIRL, LocalDate.of(2016, 1, 1))
        ))
        .type("Interim care order")
        .courtName("Newcastle Court")
        .build();

    private GeneratedOrder orderWithSingleChild = GeneratedOrder.builder()
        .dateOfIssue("2 Jun 2020")
        .children(List.of(
            testChild("George", "West", BOY, LocalDate.of(2019, 1, 1))
        ))
        .courtName("Reading Court")
        .type("Final care order")
        .build();

    private GeneratedOrder legacyOrder = GeneratedOrder.builder()
        .dateOfIssue("2 Jun 2020")
        .type("Final care order")
        .build();

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void shouldGenerateTemplateDataWhenOneOutOfMultipleCareOrdersSelected(OrderStatus orderStatus) {
        List<Integer> selectedOrders = List.of(0);
        CaseData caseData = getCase(orderStatus, selectedOrders, orderWithMultipleChildren, orderWithSingleChild);

        DocmosisGeneratedOrder actualOrderData = service.getTemplateData(caseData);

        String expectedDetails = "The court discharges the care order made by the Newcastle Court on 1 Jun 2020";
        List<DocmosisChild> expectedChildren = docmosisChild(orderWithMultipleChildren.getChildren());
        DocmosisGeneratedOrder expectedOrderData = getExpectedDocument(orderStatus, expectedDetails, expectedChildren);

        assertThat(actualOrderData).isEqualToComparingFieldByField(expectedOrderData);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void shouldGenerateTemplateDataWhenMultipleCareOrdersSelected(OrderStatus orderStatus) {
        List<Integer> selectedOrders = List.of(0, 1);
        CaseData caseData = getCase(orderStatus, selectedOrders, orderWithMultipleChildren, orderWithSingleChild);

        DocmosisGeneratedOrder actualOrderData = service.getTemplateData(caseData);

        String expectedDetails = "The court discharges:\n"
            + "The care order made by the Newcastle Court on 1 Jun 2020\n"
            + "The care order made by the Reading Court on 2 Jun 2020";
        List<DocmosisChild> expectedChildren = docmosisChild(orderWithMultipleChildren.getChildren(),
            orderWithSingleChild.getChildren());
        DocmosisGeneratedOrder expectedOrderData = getExpectedDocument(orderStatus, expectedDetails, expectedChildren);

        assertThat(actualOrderData).isEqualToComparingFieldByField(expectedOrderData);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void shouldGenerateTemplateDataWhenSingleCareOrderInACase(OrderStatus orderStatus) {
        List<Integer> selectedOrders = null;
        CaseData caseData = getCase(orderStatus, selectedOrders, orderWithMultipleChildren);

        DocmosisGeneratedOrder actualOrderData = service.getTemplateData(caseData);

        String expectedDetails = "The court discharges the care order made by the Newcastle Court on 1 Jun 2020";
        List<DocmosisChild> expectedChildren = docmosisChild(orderWithMultipleChildren.getChildren());
        DocmosisGeneratedOrder expectedOrder = getExpectedDocument(orderStatus, expectedDetails, expectedChildren);

        assertThat(actualOrderData).isEqualToComparingFieldByField(expectedOrder);
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void shouldGenerateTemplateDataWithDefaultCourtNameAndAllChildrenWhenLegacyOrder(OrderStatus orderStatus) {
        List<Integer> selectedOrders = List.of(0);
        CaseData caseData = getCase(orderStatus, selectedOrders, legacyOrder);

        DocmosisGeneratedOrder actualOrderData = service.getTemplateData(caseData);

        String expectedDetails = "The court discharges the care order made by the Family Court on 2 Jun 2020";
        List<DocmosisChild> expectedChildren = docmosisChild(caseData.getAllChildren());
        DocmosisGeneratedOrder expectedOrderData = getExpectedDocument(orderStatus, expectedDetails, expectedChildren);

        assertThat(actualOrderData).isEqualToComparingFieldByField(expectedOrderData);
    }

    CaseData getCase(OrderStatus orderStatus, List<Integer> selectedOrders, GeneratedOrder... orders) {

        CaseDataBuilder caseDataBuilder = defaultCaseData(orderStatus)
            .orderTypeAndDocument(OrderTypeAndDocument.builder()
                .type(DISCHARGE_OF_CARE_ORDER)
                .document(testDocumentReference())
                .build())
            .orderFurtherDirections(FurtherDirections.builder()
                .directionsNeeded("Yes")
                .directions("Example Directions")
                .build())
            .orderMonths(5)
            .careOrderSelector(ofNullable(selectedOrders)
                .map(selected -> CareOrderSelector.builder().selected(selected).build()).orElse(null))
            .orderCollection(wrapElements(orders));

        return caseDataBuilder.build();
    }

    private DocmosisGeneratedOrder getExpectedDocument(OrderStatus status, String details,
                                                       List<DocmosisChild> children) {
        return defaultExpectedData(DISCHARGE_OF_CARE_ORDER, status)
            .orderTitle("Discharge of care order")
            .childrenAct("Section 39(1) Children Act 1989")
            .orderDetails(details)
            .children(children)
            .build();
    }

    private List<DocmosisChild> docmosisChild(List<Element<Child>>... children) {
        return Stream.of(children).flatMap(List::stream)
            .map(Element::getValue)
            .map(Child::getParty)
            .map(child -> DocmosisChild.builder()
                .name(child.getFullName())
                .gender(child.getGender())
                .dateOfBirth(formatLocalDateToString(child.getDateOfBirth(), FormatStyle.LONG))
                .build())
            .distinct()
            .collect(Collectors.toList());
    }
}
